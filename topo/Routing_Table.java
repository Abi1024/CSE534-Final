import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Routing_Table {
    public HashMap<String,String> routing_table;
    public HashMap<String,Integer> gateways;

    public void setup_outgoing_connections(ArrayList<ObjectOutputStream> out, PrintWriter writer) throws InterruptedException{
        Iterator it = gateways.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String forwardName = (String)pair.getKey();
            int forwardport = forwardName.hashCode()%5000 + 6001;
            writer.println("Connecting to " + forwardName +
                    " on port " + forwardport);
            boolean connected = false;
            while(!connected){
                try{
                    Socket client = new Socket(forwardName, forwardport);
                    writer.println("Just connected to "
                            + client.getRemoteSocketAddress());
                    OutputStream outToServer = client.getOutputStream();
                    out.set((int)pair.getValue(),new ObjectOutputStream(outToServer));
                    connected = true;
                }catch(Exception e){
                    writer.println("Can't connect. Attempting to reconnect in 500 ms");
                    Thread.sleep(500);
                }
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
