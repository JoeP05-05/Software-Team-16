import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;


public class udpClient()
{

    public static void main(String args[])
    {

        Scanner photonClient = new Scanner(System.in);


        //Creates the socket for the clients
        Datagram photonCS = new DatagramSocket();

        InetAddress ip = InetAddress.getLocalHost();
        byte buf[] = null;

        while(true)
        {
            

            buf = inp.getBytes();

            DatagramPacket photonSend = new DatagramPacket(buf, buf.length, ip, 7500);

            photonCS.send(photonSend);

            if (inp)
        }

  
    }
}
