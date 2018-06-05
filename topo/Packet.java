import java.io.Serializable;

public class Packet implements Serializable {
    int payload;
    String source;
    String destination;
    long timeStamp;

    Packet(int payload, String source, String destination, long timeStamp){
        this.payload = payload;
        this.source = source;
        this.destination = destination;
        this.timeStamp = timeStamp;
    }
}
