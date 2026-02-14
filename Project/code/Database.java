import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/photon";

        // Allow overriding DB credentials via environment variables:
        // Set DB_USER and DB_PASSWORD before running the JVM (recommended).
        private static final String USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER") : "postgres";
        private static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD") : "";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found on classpath.\n" +
                    "Download the driver (org.postgresql:postgresql) and run with -cp including the jar.");
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initialize() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            String sql =
                    "CREATE TABLE IF NOT EXISTS players (\n" +
                    "    id SERIAL PRIMARY KEY,\n" +
                    "    name VARCHAR(100),\n" +
                    "    team VARCHAR(20),\n" +
                    "    score INT DEFAULT 0\n" +
                    ");";

            stmt.execute(sql);
            System.out.println("Database initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
