import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


public class udpClient
{

    public static void main(String args[])
    {

        Scanner photonClient = new Scanner(System.in);

		InetAddress ip = null;

		while (true)
		{
			System.out.println("Enter IP address: ");

			String ipInput = photonClient.nextLine();

			try
			{
				ip = InetAddress.getByName(ipInput);
				break;
			} 
			catch (Exception e)
			{
				System.out.println("Error: Invalid IP address, please input correct address.");
			}
		}
		

		

        //Creates the socket for the clients
        Datagram ds = new DatagramSocket(7500, ip);

        
        byte[] buf;

        while (true)
		{
			String inp = photonClient.nextLine();

			// convert the String input into the byte array.
			buf = inp.getBytes();

			// Step 2 : Create the datagramPacket for sending
			// the data.
			DatagramPacket DpSend =
				new DatagramPacket(buf, buf.length, ip, 7501);

			// Step 3 : invoke the send call to actually send
			// the data.
			ds.send(DpSend);

			// break the loop
			if (inp.equals("bye"))
				break;
		}

  
    }
}
