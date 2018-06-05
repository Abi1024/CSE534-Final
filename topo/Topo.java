import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class Topo {
    //this function takes an IP address and returns the name of the node associated with that IP
    public static String ip_to_node_name(String ip){
        switch (ip){
            case "1.1.1.1":
                return "H1";
            case "1.1.1.2":
                return "R1";
            case "1.1.2.2":
                return "H2";
            default:
                return "TROLL";
        }
    }

    /*this is the forwarding table for each node inthe topology. Basically, if given a node A, and we know that in order to get from A to C, A must first go to its neighbor B, we add the entry
    router.routing_table.put("C eth0 IP","B eth0 IP") for the section A eth0 IP. For each unique gateway router, we also add it to the gateways set.

    */

    public static void setup_routing_table(String this_ip, Routing_Table router, HashMap<String,ObjectOutputStream> out, PrintWriter writer) throws Exception{
        switch(this_ip){
            case "1.1.1.1":
                router.routing_table.put("1.1.2.2","1.1.1.2");
                router.routing_table.put("1.1.1.2","1.1.1.2");
                router.gateways.add("1.1.1.2");
                break;
            case "1.1.1.2":
                router.routing_table.put("1.1.2.2","1.1.2.2");
                router.gateways.add("1.1.1.1");
                router.gateways.add("1.1.2.2");
                router.routing_table.put("1.1.1.1","1.1.1.1");
                break;
            case "1.1.2.2":
                router.routing_table.put("1.1.1.1","1.1.1.2");
                router.routing_table.put("1.1.1.2","1.1.1.2");
                router.gateways.add("1.1.1.2");
                break;
            default:
                break;
        }
        writer.println("CLIENT: Size of gateways: " + router.gateways.size());
        writer.println("CLIENT: Size of out: " + out.size());
        router.setup_outgoing_connections(out, writer);
    }
}
