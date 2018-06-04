import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

public class Host {
    private static ServerSocket serverSocket;
    public static Thread t1, t2;
    public static boolean is_sending = false; //if this is false, it means this host is not sending any packets.
    public static String node_name;
    public static String this_ip;
    public static int server_port;
    public static PrintWriter writer;
    public static Routing_Table router = new Routing_Table();
    public static ArrayList<ObjectOutputStream> out;

    public static void thread1(String destName, int destport, double packets_per_second) {
        Random rand = new Random();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setup_routing_table();
                    writer.println("Connecting to " + destName +
                            " on port " + destport);
                    Socket client = new Socket(destName, destport);
                    writer.println("Just connected to "
                            + client.getRemoteSocketAddress());
                    OutputStream outToServer = client.getOutputStream();
                    //@TODO: Add Bursty traffic
                    for (int i = 0; i < 1000; i++) {
                        Integer data = rand.nextInt(999999) + 1;
                        long time = System.currentTimeMillis();
                        out.get(router.gateways.get(destName)).writeObject(new Packet(data,destName,time));
                        Thread.sleep((int) (1000.0 / packets_per_second));
                    }
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setup_routing_table() throws InterruptedException{
        if (this_ip == "1.1.1.1"){
            router.routing_table.put("1.1.2.2","1.1.1.2");
            router.routing_table.put("1.1.1.2","1.1.1.2");
            router.gateways.put("1.1.1.2",0);
        }else if (this_ip == "1.1.2.2"){
            router.routing_table.put("1.1.1.1","1.1.1.2");
            router.routing_table.put("1.1.1.2","1.1.1.2");
            router.gateways.put("1.1.1.2",0);
        }

        out = new ArrayList<>(router.gateways.size());
        router.setup_outgoing_connections(out, writer);
    }

    public static void thread2() {
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    this_ip = Get_IP.get_ip();
                    server_port = this_ip.hashCode()%5000+6001;
                    node_name = Get_IP.ip_to_node_name(this_ip);
                    writer = new PrintWriter(node_name +"_ERROR.txt", "UTF-8");
                    serverSocket = new ServerSocket(server_port);
                    serverSocket.setSoTimeout(10000000);
                    while(true){
                        try{
                            writer.println("Waiting for client on port " +
                                    serverSocket.getLocalPort() + "...");
                            Socket server = serverSocket.accept();
                            writer.println("Just connected to "
                                    + server.getRemoteSocketAddress());
                            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
                            boolean flag = true;
                            while(true){
                                Packet input = (Packet)in.readObject();
                                if (input != null){
                                    writer.println("Receiving packet at host: " + input.payload);
                                }else{
                                    flag = false;
                                }
                            }
                        }catch(SocketTimeoutException s) {
                            writer.println("Socket timed out!");
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

    public static void main(String[] args) throws InterruptedException {
        String destName = "1.1.2.2";
        double packets_per_second = 1;
        if (args.length >= 1) {
            is_sending = true;
            destName = args[0];
            if (args.length > 1) {
                packets_per_second = Double.parseDouble(args[1]);
            }
        }
        int destport = destName.hashCode()%5000+6001;
        if (is_sending) {
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

