import java.io.Serializable;

public class Packet implements Serializable {
    int payload;
    String destination;
    long timeStamp;

    Packet(int payload, String destination, long timeStamp){
        this.payload = payload;
        this.destination = destination;
        this.timeStamp = timeStamp;
    }
}
