import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Routing_Table {
    public  HashMap<String,String> routing_table = new HashMap<>();
    public Set<String> gateways = new HashSet<>();

    public void setup_outgoing_connections(HashMap<String,ObjectOutputStream> out, PrintWriter writer) throws InterruptedException{
        writer.println("CLIENT: IN function, setup_outgoing_connections");
        for (String forwardName : gateways) {
            int forwardport = forwardName.hashCode()%5000 + 6001;
            writer.println("CLIENT: Connecting to " + forwardName +
                    " on port " + forwardport);
            writer.println("CLIENT: Pair key: " + forwardName);
            boolean connected = false;
            while(!connected){
                try{
                    Socket client = new Socket(forwardName, forwardport);
                    OutputStream outToServer = client.getOutputStream();
                    out.put(forwardName,new ObjectOutputStream(outToServer));
                    connected = true;
                    writer.println("CLIENT: Just connected to "
                            + client.getRemoteSocketAddress());
                }catch(Exception e){
                    writer.println("CLIENT: Can't connect. Attempting to reconnect in 500 ms");
                    e.printStackTrace(writer);
                    Thread.sleep(500);
                }
            }
        }
    }
}
