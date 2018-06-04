import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Get_IP {
    public static String get_ip() {
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
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return "2.2.2.2";
    }

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
}
