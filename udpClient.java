import java.net.*;
import java.util.Scanner;

public class udpClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("UDP Client - Send messages to server");
        System.out.println("Format: senderId:hitId (e.g., 123:456)");
        System.out.println("Type 'quit' to exit\n");
        
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName("127.0.0.1");
            int serverPort = 7501; // Send to receiver port
            
            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine();
                
                if (message.equalsIgnoreCase("quit")) {
                    break;
                }
                
                byte[] buf = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddress, serverPort);
                socket.send(packet);
                System.out.println("Sent: " + message);
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        scanner.close();
    }
}
