import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client{
    public static void main(String [] args){
        Random rand = new Random();
        String serverName = "127.0.0.1";
        //tring serverName = "1.1.1.2";
        int port = 6006; //port
        try {
	         System.out.println("Connecting to " + serverName +
			 " on port " + port);
	         Socket client = new Socket(serverName, port);
	         System.out.println("Just connected to "
			 + client.getRemoteSocketAddress());
	         OutputStream outToServer = client.getOutputStream();
	         DataOutputStream out = new DataOutputStream(outToServer);
	         for (int i = 0; i < 1000; i++){
	             String data = Integer.toString(rand.nextInt(999999) + 1);
                 Thread.sleep(1000);
                 out.writeUTF(data);
             }
	         //InputStream inFromServer = client.getInputStream();
	         //DataInputStream in = new DataInputStream(inFromServer);
	         //System.out.println("Server says " + in.readUTF());
	         //write these to a file weight.txts
	         client.close();
	      }catch(Exception e) {
	         e.printStackTrace();
	      }
	   }
}

