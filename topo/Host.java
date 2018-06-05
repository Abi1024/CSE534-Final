import java.io.*;
import java.net.*;
import java.util.*;

public class Host {
    private static ServerSocket serverSocket;
    public static Thread t1, t2;
    public static boolean is_sending = false; //if this is false, it means this host is not sending any packets.
    public static String node_name;
    public static String this_ip;
    public static int server_port;
    public static PrintWriter writer;
    public static Routing_Table router = new Routing_Table();
    public static HashMap<String, ObjectOutputStream> out = new HashMap<>();

    //Host's thread1 is responsible for sending out generated traffic
    public static void thread1(String destName, int destport, double packets_per_second) {
        Random rand = new Random();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setup_routing_table();
                    //@TODO: Add Bursty traffic
                    for (int i = 0; i < 1000; i++) {
                        Integer data = rand.nextInt(999999) + 1;
                        long time = System.currentTimeMillis();
                        writer.println("CLIENT: The gateway router is: " + router.routing_table.get(destName));
                        out.get(router.routing_table.get(destName)).writeObject(new Packet(data,destName,time));
                        Thread.sleep((int) (1000.0 / packets_per_second));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setup_routing_table() throws Exception{
        if (this_ip.equals("1.1.1.1")){
            router.routing_table.put("1.1.2.2","1.1.1.2");
            router.routing_table.put("1.1.1.2","1.1.1.2");
            router.gateways.add("1.1.1.2");
        }else if (this_ip.equals("1.1.2.2")){
            router.routing_table.put("1.1.1.1","1.1.1.2");
            router.routing_table.put("1.1.1.2","1.1.1.2");
            router.gateways.add("1.1.1.2");
        }
        writer.println("CLIENT: Size of gateways: " + router.gateways.size());
        writer.println("CLIENT: Size of out: " + out.size());
        router.setup_outgoing_connections(out, writer);
    }

    //Host's thread2 is the server, i.e. the host listens for incoming packets
    public static void thread2() {
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(true){
                        try{
                            writer.println("SERVER: Waiting for client on port " +
                                    serverSocket.getLocalPort() + "...");
                            Socket server = serverSocket.accept();
                            writer.println("SERVER: Just connected to "
                                    + server.getRemoteSocketAddress());
                            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
                            boolean flag = true;
                            while(true){
                                Packet input = (Packet)in.readObject();
                                if (input != null){
                                    System.out.println("SERVER: Receiving packet at host: " + input.payload);
                                }else{
                                    flag = false;
                                }
                                Thread.sleep(1000);
                            }
                        }catch(SocketTimeoutException s) {
                            writer.println("SERVER: Socket timed out!");
                            break;
                        }catch(IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void initialize() throws Exception{
        this_ip = Get_IP.get_ip();
        server_port = this_ip.hashCode()%5000+6001;
        node_name = Get_IP.ip_to_node_name(this_ip);
        File file = new File(node_name +"_ERROR.txt");
        if(!file.exists()) file.createNewFile();;
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        writer = new PrintWriter(bw, true);
        writer.println("THIS NODE's IP: " + this_ip);
        writer.println("THIS NODE's SERVER PORT: " + server_port);
        writer.println("THIS NODE's NAME: " + node_name);
        serverSocket = new ServerSocket(server_port);
        serverSocket.setSoTimeout(10000000);
    }

    public static void main(String[] args) throws Exception {
        String destName = "XXXXXXX";
        double packets_per_second = 1;
        if (args.length >= 1) {
            is_sending = true;
            destName = args[0];
            if (args.length > 1) {
                packets_per_second = Double.parseDouble(args[1]);
            }
        }
        int destport = destName.hashCode()%5000+6001;
        initialize();
        if (is_sending) {
            writer.println("Destination IP: " + destName);
            writer.println("Destination Port: " + destport);
            writer.println("Packs per second: " + packets_per_second);
            thread1(destName, destport, packets_per_second);
        }
        thread2();
        if (is_sending) {
            t1.start();
        }
        t2.start();
        if (is_sending) {
            t1.join();
        }
        t2.join();
    }

}

