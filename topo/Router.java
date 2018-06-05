
import java.net.*;
import java.util.*;
import java.io.*;

public class Router{
    private static ServerSocket serverSocket;
    public static String node_name;
    public static String this_ip;
    public static int server_port;
    public static PrintWriter writer;
    public static Thread t1,t2;
    public static final PC pc = new PC();
    public static HashMap<String, ObjectOutputStream> out = new HashMap<>();
    public static Routing_Table router = new Routing_Table();

    //producer-consumer
    private static class PC{
        private static Queue<Packet> queue = new LinkedList<>();
        public static final int max_size = 100;

        public void produce(Packet input) throws InterruptedException {
            synchronized (this){
                if (queue.size() < max_size){
                    writer.println("PRODUCER: Adding input to queue: " + input + " Queue now has size: " + queue.size());
                    queue.add(input);
                }else{
                    writer.println("PRODUCER: Full queue. Dropping packet: " + input);
                }
            }
        }
        public void consume() throws InterruptedException{
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
                Thread.sleep(5000);
            }
        }
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

    public static void main(String[] args) throws Exception{
        initialize();
        thread1();
        thread2();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    public static void setup_routing_table() throws InterruptedException{
        router.routing_table.put("1.1.2.2","1.1.2.2");
        router.gateways.add("1.1.1.1");
        router.gateways.add("1.1.2.2");
        router.routing_table.put("1.1.1.1","1.1.1.1");
        router.setup_outgoing_connections(out, writer);
    }

    public static void thread1(){
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
                                    pc.produce(input);
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
    public static void thread2(){
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    setup_routing_table();
                    pc.consume();
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }
}