
import java.net.*;
import java.util.*;
import java.io.*;

public class Router{
    private static ServerSocket serverSocket;
    public static String node_name;
    public static String this_ip;
    public static int server_port;
    public static PrintWriter writer;
    public static PrintWriter graph;
    public static Thread t1,t2;
    public static final PC pc = new PC();
    public static HashMap<String, ObjectOutputStream> out = new HashMap<>();
    public static Routing_Table router = new Routing_Table();
    public static long start_time = System.currentTimeMillis();
    public static long total_packets,dropped_packets;
    public static long last_time = System.currentTimeMillis();;
    public static long max_queue_size = 100;

    //producer-consumer
    private static class PC{
        private static Queue<Packet> queue = new LinkedList<>();

        public void produce(Packet input, long measurement_interval) throws InterruptedException {
            total_packets++;
            synchronized (this){
                writer.println("PRODUCER: Receiving packet. Time: " + (System.currentTimeMillis()-start_time));
                if (queue.size() < max_queue_size){
                    writer.println("PRODUCER: Adding input to queue: " + input + " Queue now has size: " + queue.size());
                    queue.add(input);
                }else{
                    writer.println("PRODUCER: Full queue. Dropping packet: " + input);
                    dropped_packets++;
                }
                if (System.currentTimeMillis() - last_time > measurement_interval){
                    writer.println("PRODUCER: WRITING TO GRAPH");
                    graph.println(System.currentTimeMillis()-start_time + "," + queue.size() + "," + ((double)dropped_packets/total_packets));
                    last_time = System.currentTimeMillis();
                }
            }
        }
        public void consume(double link_rate) throws InterruptedException{

            while (true) {
                synchronized (this) {
                    // consumer thread waits while list
                    // is empty
                    if (!queue.isEmpty()) {
                        Packet x = queue.remove();
                        //writer.println("CONSUMER: Queue has: " + queue.size() + " objects. Just removed: " + Integer.toString(x));
                        try{
                            writer.println("CLIENT (CONSUMER): The gateway router is:" + router.routing_table.get(x.destination));
                            out.get(router.routing_table.get(x.destination)).writeObject(x);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }else{
                        writer.println("CONSUMER: QUEUE IS EMPTY");
                    }
                }
                Thread.sleep((int)(1000.0/link_rate));
            }
        }
    }

    

    public static void initialize() throws Exception{
        this_ip = Get_IP.get_ip();
        server_port = this_ip.hashCode()%5000+6001;
        node_name = Topo.ip_to_node_name(this_ip);
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

        File file2 = new File("queue.csv");
        if(!file2.exists()) file2.createNewFile();;
        FileOutputStream fos2 = new FileOutputStream(file2, false);
        OutputStreamWriter osw2 = new OutputStreamWriter(fos2, "UTF-8");
        BufferedWriter bw2 = new BufferedWriter(osw2);
        graph = new PrintWriter(bw2, true);
        graph.println("Time since start (ms),Queue size,Average packet loss");

    }

    public static void main(String[] args) throws Exception{
        double link_rate = 0.2;
        long measurement_interval = 1000;
        if (args.length >= 1) {
            link_rate = Double.parseDouble(args[0]);
            if (args.length >= 2) {
                measurement_interval = Long.parseLong(args[1]);
                max_queue_size = Integer.parseInt(args[2]);
            }
        }
        initialize();
        writer.println("Link rate: " + link_rate);
        thread1(measurement_interval);
        thread2(link_rate);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    public static void thread1(long measurement_interval){
        t1 = new Thread(new Runnable() {
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
                            while (true){
                                Packet input = (Packet)in.readObject();
                                if (input != null){
                                    pc.produce(input, measurement_interval);
                                }else{
                                    flag = false;
                                }
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

    //Router's thread2 is responsible for taking packets in the queue and sending them on their way to the next gateway router (or to the destination if it is one hop away)
    public static void thread2(double link_rate){
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Topo.setup_routing_table(this_ip,router,out,writer);
                    pc.consume(link_rate);
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }
}