//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
//Team: Team 16 
import java.net.*;

public class udpServer {
    public static void main(String[] args) {
        System.out.println("UDP Server listening on port 7501...");
        System.out.println("Press Ctrl+C to stop\n");
        
        try (DatagramSocket socket = new DatagramSocket(7501, InetAddress.getByName("0.0.0.0"))) {
            byte[] buffer = new byte[1024];
            
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received from " + packet.getAddress() + ":" + packet.getPort() + 
                                 " - " + received);
                
                // Echo back to sender (optional)
                // DatagramPacket response = new DatagramPacket(
                //     packet.getData(), packet.getLength(), packet.getAddress(), packet.getPort());
                // socket.send(response);
                
                // Clear buffer
                buffer = new byte[1024];
            }
            
        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
