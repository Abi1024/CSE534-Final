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
    public static long start_time = System.currentTimeMillis();
    public static double upload_rate;

    //Host's thread1 is responsible for sending out generated traffic
    public static void thread1(String destName, int destport,int num_packets) {
        Random rand = new Random();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Topo2.setup_routing_table(this_ip,router,out,writer);
                    double probability = 0;
                    synchronized ((Double)upload_rate){
                        probability = ((float)(2*upload_rate+1-Math.sqrt(4*upload_rate+1)))/(2*upload_rate);
                    }
                    writer.println("Probability: " + probability);
                    for (int i = 0; i < num_packets; i++) {
                        //writer.println("Next iteration");
                        while (rand.nextInt(100) <= probability*100){
                            //writer.println("OK: " + (probability*100));
                            Integer data = rand.nextInt(999999) + 1;
                            long time = System.currentTimeMillis();
                            writer.println("Sending packet, time: " + (time-start_time));
                            out.get(router.routing_table.get(destName)).writeObject(new Packet(data,this_ip,destName,time));
                            //writer.println("Packet sent, time: " + (time-start_time));
                        }
                        //writer.println("Delay: " + (int) (1000.0 / upload_rate));
                        double rate = 1;
                        synchronized ((Double)upload_rate){
                            rate = upload_rate;
                            probability = ((float)(2*upload_rate+1-Math.sqrt(4*upload_rate+1)))/(2*upload_rate);
                        }
                        Thread.sleep((int) (1000.0 / rate));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    //Host's thread2 is the server, i.e. the host listens for incoming packets
    public static void thread2(double download_rate) {
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
                                    long time = System.currentTimeMillis() - input.timeStamp;
                                    System.out.println("SERVER: Receiving packet with payload: " + input.payload + " Source: " + input.source + " Destination: " + input.destination + " Time elapsed (ms): " + time );
                                    if (input.payload == -1){
                                        synchronized ((Double)upload_rate){
                                            upload_rate *= .99;
                                            writer.println("SERVER: THROTTLING UPLOAD. Upload rate: " + upload_rate);
                                        }
                                    }
                                }else{
                                    flag = false;
                                }
                                Thread.sleep((int)(1000.0/download_rate));
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
        node_name = Topo2.ip_to_node_name(this_ip);
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
        upload_rate = 1;
        double download_rate = 1;
        int num_packets = 1000;
        if (args.length >= 1) {
            download_rate = Double.parseDouble(args[0]);
            if (args.length >= 2){
                is_sending = true;
                destName = args[1];
                if (args.length >= 3){
                    upload_rate = Double.parseDouble(args[2]);
                    num_packets = Integer.parseInt(args[3]);
                }
            }
        }
        int destport = destName.hashCode()%5000+6001;
        initialize();
        if (is_sending) {
            writer.println("Destination IP: " + destName);
            writer.println("Destination Port: " + destport);
            writer.println("Packs per second: " + upload_rate);
            thread1(destName, destport,num_packets);
        }
        thread2(download_rate);
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

