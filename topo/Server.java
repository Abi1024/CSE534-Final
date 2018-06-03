
import java.net.*;
import java.util.Enumeration;
import java.io.*;

public class Server extends Thread {
    private ServerSocket serverSocket;
    public static int node_id;
    public static int server_port = 6006;
    static String filename;
    static String next_hop_ip;
    static int next_hop_port;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000000);
    }

    private class Node {
        private static final int H1 = 0;
        private static final int R1 = 1;
        private static final int H2 = 5;
    }

    public void run() {
	      while(true) {
	         try {
	            System.out.println("Waiting for client on port " +
	            serverSocket.getLocalPort() + "...");
	            Socket server = serverSocket.accept();
	            System.out.println("Just connected to "
	                  + server.getRemoteSocketAddress());
	            DataInputStream in =
	                  new DataInputStream(server.getInputStream());
	            System.out.println(in.readUTF());

                 OutputStream outToClient = server.getOutputStream();
                 OutputStream outToServer = server.getOutputStream();
                 DataOutputStream out = new DataOutputStream(outToServer);
                 out.writeUTF("nigger"); // change in weight in the correct format
	            server.close();
	         }catch(SocketTimeoutException s)
	         {
	            System.out.println("Socket timed out!");
	            break;
	         }catch(IOException e)
	         {
	            e.printStackTrace();
	            break;
	         }
	      }
    }

    public static void main(String[] args) {
        identify_node();
        try {
            Thread t = new Server(server_port);
            t.start();
        }catch(IOException e) {
            e.printStackTrace();
        }
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
