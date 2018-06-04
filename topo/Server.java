
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Enumeration;
import java.io.*;

public class Server{
    private static ServerSocket serverSocket;
    public static int node_id;
    public static int server_port = 6006;
    public static Thread t1,t2;
    public static final PC pc = new PC();

    private class Node {
        private static final int H1 = 0;
        private static final int R1 = 1;
        private static final int H2 = 2;
    }

    //producer-consumer
    private static class PC{
        private static Queue<Integer> queue = new LinkedList<>();
        public static final int max_size = 100;

        public void produce(String input) throws InterruptedException {
            synchronized (this){
                if (queue.size() < max_size){
                    System.out.println("PRODUCE: Adding input to queue: " + input + "Queue now has size: " + queue.size());
                    queue.add(Integer.parseInt(input));
                }else{
                    System.out.println("Full queue. Dropping packet: " + input);
                }
            }
        }
        public void consume() throws InterruptedException {
            while (true) {
                synchronized (this) {
                    // consumer thread waits while list
                    // is empty
                    if (!queue.isEmpty()) {
                        Integer x = queue.remove();
                        System.out.println("CONSUMER: Queue has: " + queue.size() + " objects. Just removed: " + Integer.toString(x));
                    }else{
                        System.out.println("CONSUMER: QUEUE IS EMPTY");
                    }
                }
                Thread.sleep(5000);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException{
        identify_node();
        thread1();
        thread2();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    public static void thread1(){
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    serverSocket = new ServerSocket(server_port);
                    serverSocket.setSoTimeout(10000000);
                    while(true) {
                        try{
                            System.out.println("Waiting for client on port " +
                                    serverSocket.getLocalPort() + "...");
                            Socket server = serverSocket.accept();
                            System.out.println("Just connected to "
                                    + server.getRemoteSocketAddress());
                            DataInputStream in = new DataInputStream(server.getInputStream());
                            boolean flag = true;
                            while (true){
                                String input = in.readUTF();
                                if (input.length() > 0){
                                    pc.produce(input);
                                }else{
                                    flag = false;
                                }
                            }

                            //OutputStream outToClient = server.getOutputStream();
                            // OutputStream outToServer = server.getOutputStream();
                            //DataOutputStream out = new DataOutputStream(outToServer);
                            //out.writeUTF("nigger"); // change in weight in the correct format
                            //server.close();
                        }catch(SocketTimeoutException s) {
                            System.out.println("Socket timed out!");
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

    public static void thread2(){
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    pc.consume();
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }


    public static void identify_node() {
        try {
            String ip;
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.getName().contains("eth0")) {
                    Enumeration<InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress addr = addresses.nextElement();
                        System.out.println(addr);
                        ip = addr.getHostAddress();
                        if (ip.contains(".")) {
                            switch (ip) {
                                case "1.1.1.1":
                                    node_id = Node.H1;
                                    server_port = 6001;
                                    break;
                                case "1.1.1.2":
                                    node_id = Node.R1;
                                    server_port = 6002;
                                    break;
                                case "1.1.2.2":
                                    node_id = Node.H2;
                                    server_port = 6003;
                                    break;
                                default:
                                    System.out.println("Didn't find a matching node!");
                                    server_port = 6006;
                                    break;
                            }
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
}
        /*int port = 1024; //port number args[2] =next hop ip args[3] = next hop port
        next_hop_ip = "127.0.0.1";
        next_hop_port = 1044;
        filename = "H1.txt"; //change in weight
        */
