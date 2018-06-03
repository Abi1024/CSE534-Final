import java.io.*;
import java.net.Socket;
public class Client {
	   public static void main(String [] args) // three arguments required here
	   {
	      String serverName = "172.24.224.169";// ip
	      int port = 6006; //port
	      try
	      {
	         System.out.println("Connecting to " + serverName +
			 " on port " + port);
	         Socket client = new Socket(serverName, port);
	         System.out.println("Just connected to "
			 + client.getRemoteSocketAddress());
	         OutputStream outToServer = client.getOutputStream();
	         DataOutputStream out = new DataOutputStream(outToServer);
	         out.writeUTF("faggot"); // change in weight in the correct format
	         InputStream inFromServer = client.getInputStream();
	         DataInputStream in =
	                        new DataInputStream(inFromServer);
	         System.out.println("Server says " + in.readUTF());
	         //write these to a file weight.txts
	         client.close();
	      }catch(IOException e)
	      {
	         e.printStackTrace();
	      }
	   }
}
