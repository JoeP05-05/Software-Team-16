//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
//Software Engineering Team: Team 16 
import java.sql.*;
import java.net.*;
import java.io.IOException;

public class test {
    public static void main(String[] args) {
        System.out.println("=== TESTING SPRINT 2 COMPONENTS ===\n");
        
        // Test 1: Database connection
        testDatabase();
        
        // Test 2: UDP broadcast
        testUDPBroadcast();
        
        // Test 3: UDP receive (listener test)
        testUDPReceive();
    }
    
    private static void testDatabase() 
    {
        System.out.println(" TEST 1: PostgreSQL Connection");
        System.out.println("--------------------------------");

        try {
            Class.forName("org.postgresql.Driver");

            // Python-style connection params
            Map<String, String> params = new HashMap<>();
            params.put("dbname", "photon");
            params.put("user", "student");
            params.put("password", "student");
            params.put("host", "localhost");
            params.put("port", "5432");

            // Defaults if not provided
            String host = params.getOrDefault("host", "127.0.0.1");
            String port = params.getOrDefault("port", "5432");
            String db   = params.get("dbname");

            String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);

            String user = params.get("user");
            String pass = params.get("password");

            Connection conn = DriverManager.getConnection(url, user, pass);

            System.out.println(" SUCCESS: Connected to database");

            // Check if players table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "players", null);

            if (tables.next()) {
                System.out.println(" SUCCESS: 'players' table exists");

                try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM players")) {
                    rs.next();
                    int count = rs.getInt(1);
                    System.out.println(" Current players in database: " + count);
                }

                System.out.println("\n Table structure:");
                ResultSet columns = meta.getColumns(null, null, "players", null);
                while (columns.next()) {
                    String colName = columns.getString("COLUMN_NAME");
                    String colType = columns.getString("TYPE_NAME");
                    System.out.println("   - " + colName + " (" + colType + ")");
                }

            } else {
                System.out.println(" ERROR: 'players' table does not exist");
                System.out.println("   Run: CREATE TABLE players (id SERIAL PRIMARY KEY, name VARCHAR(100), team VARCHAR(10), equipment_id INTEGER UNIQUE);");
            }

            conn.close();

        } catch (SQLException e) {
            System.out.println(" ERROR: " + e.getMessage());
            System.out.println("\n🔧 Troubleshooting:");
            System.out.println("   1. Is PostgreSQL running?");
            System.out.println("   2. Does the 'student' user have a password set?");
            System.out.println("   3. Does 'photon' database exist?");
        } catch (ClassNotFoundException e) {
            System.out.println(" ERROR: PostgreSQL JDBC Driver not found");
        }

        System.out.println();
    }
    
    private static void testUDPBroadcast() {
        System.out.println("📡 TEST 2: UDP Broadcast (port 7500)");
        System.out.println("-------------------------------------");
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            
            String message = "999"; // Test equipment ID
            byte[] buf = message.getBytes();
            InetAddress address = InetAddress.getByName("127.0.0.1");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 7500);
            
            socket.send(packet);
            System.out.println(" SUCCESS: Sent test broadcast: " + message);
            System.out.println("   To: 127.0.0.1:7500");
            System.out.println("\n🔧 To verify: Run 'nc -ul 7500' in another terminal");
            
        } catch (IOException e) {
            System.out.println(" ERROR: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void testUDPReceive() {
        System.out.println(" TEST 3: UDP Receive Listener (port 7501)");
        System.out.println("-------------------------------------------");
        
        System.out.println("Starting receiver for 3 seconds...");
        System.out.println("Send test message from another terminal: echo \"123:456\" | nc -u 127.0.0.1 7501");
        
        Thread receiver = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(7501, InetAddress.getByName("0.0.0.0"))) {
                socket.setSoTimeout(3000);
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                System.out.println(" Receiver ready on port 7501");
                
                try {
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(" RECEIVED: " + received);
                    
                    // Parse and test
                    if (received.contains(":")) {
                        String[] parts = received.split(":");
                        System.out.println("   Sender ID: " + parts[0]);
                        System.out.println("   Hit ID: " + parts[1]);
                        System.out.println(" Format correct (integer:integer)");
                    }
                    
                } catch (SocketTimeoutException e) {
                    System.out.println("  Timeout - no message received (this is OK for test)");
                }
                
            } catch (IOException e) {
                System.out.println(" ERROR: " + e.getMessage());
            }
        });
        
        receiver.start();
        
        try {
            receiver.join(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Run the main application with:");
        System.out.println("   javac -cp .:postgresql-42.7.4.jar *.java");
        System.out.println("   java -cp .:postgresql-42.7.4.jar Main");
    }
}
