//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
//Team: Team 16 
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.net.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Player_Entry extends JFrame {

    private static final int MAX_PLAYERS_PER_TEAM = 15;
    private static final int BROADCAST_PORT = 7500;
    private static final int RECEIVE_PORT = 7501;

    private JPanel greenTeamPanel;
    private JPanel redTeamPanel;

    private JTextField[][] greenFields = new JTextField[MAX_PLAYERS_PER_TEAM][3];
    private JTextField[][] redFields = new JTextField[MAX_PLAYERS_PER_TEAM][3];

    private Map<Integer, String> playerDatabase = new HashMap<>();
    private Set<Integer> usedEquipmentIds = new HashSet<>();

    private Connection dbConnection;

    private DatagramSocket broadcastSocket;
    private DatagramSocket receiveSocket;
    private String broadcastIp = "127.0.0.1";
    private Thread receiveThread;
    private volatile boolean receiving = false;

    private JButton startButton;
    private JButton clearAllButton;
    private JButton settingsButton;
    private JLabel statusLabel;
    private JLabel greenCountLabel;
    private JLabel redCountLabel;

    public Player_Entry() {
        initDatabase();
        initUDP();

        setTitle("Player Entry - Software Engineering Sprint 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createTeamPanels(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        addKeyBindings();
        updateTeamCounts();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("PLAYER ENTRY SCREEN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(50, 50, 150));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        settingsButton = new JButton(" Settings");
        settingsButton.setFont(new Font("Arial", Font.PLAIN, 14));
        settingsButton.addActionListener(e -> showSettingsDialog());
        headerPanel.add(settingsButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTeamPanels() {
        JPanel teamPanelContainer = new JPanel(new GridLayout(1, 2, 20, 0));

        greenTeamPanel = createTeamPanel("GREEN TEAM", new Color(74, 163, 117), greenFields);
        redTeamPanel   = createTeamPanel("RED TEAM",   new Color(204, 75, 86),  redFields);

        teamPanelContainer.add(greenTeamPanel);
        teamPanelContainer.add(redTeamPanel);

        return teamPanelContainer;
    }

    private JPanel createTeamPanel(String teamName, Color bgColor, JTextField[][] fields) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel teamHeader = new JLabel(teamName, SwingConstants.CENTER);
        teamHeader.setFont(new Font("Arial", Font.BOLD, 20));
        teamHeader.setForeground(Color.WHITE);
        teamHeader.setOpaque(true);
        teamHeader.setBackground(bgColor.darker());
        teamHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(teamHeader, BorderLayout.NORTH);

        String[] columnNames = {"Player ID", "Equipment ID", "Code Name"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 2;
            }
        };

        for (int i = 0; i < MAX_PLAYERS_PER_TEAM; i++) {
            model.addRow(new Object[]{"", "", ""});
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Monospaced", Font.PLAIN, 14));

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);

        JTextField textField = new JTextField();
        DefaultCellEditor editor = new DefaultCellEditor(textField);
        table.getColumnModel().getColumn(0).setCellEditor(editor);
        table.getColumnModel().getColumn(1).setCellEditor(editor);

        table.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int col = e.getColumn();
            if (col == 0) {
                handlePlayerIdEntry(table, row, teamName);
            } else if (col == 1) {
                handleEquipmentIdEntry(table, row, teamName);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void handlePlayerIdEntry(JTable table, int row, String teamName) {
        String playerIdStr = (String) table.getValueAt(row, 0);
        if (playerIdStr == null || playerIdStr.trim().isEmpty()) {
            return;
        }

        try {
            int playerId = Integer.parseInt(playerIdStr.trim());
            String codeName = getCodeNameFromDatabase(playerId);

            if (codeName != null) {
                table.setValueAt(codeName, row, 2);
                table.setValueAt("", row, 1);
                JOptionPane.showMessageDialog(this,
                    "Player " + playerId + " found: " + codeName,
                    "Player Found",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                String newCodeName = JOptionPane.showInputDialog(this,
                    "Player ID " + playerId + " not found.\nEnter code name for new player:",
                    "New Player",
                    JOptionPane.QUESTION_MESSAGE);

                if (newCodeName != null && !newCodeName.trim().isEmpty()) {
                    addPlayerToDatabase(playerId, newCodeName.trim());
                    table.setValueAt(newCodeName.trim(), row, 2);
                    playerDatabase.put(playerId, newCodeName.trim());
                    JOptionPane.showMessageDialog(this,
                        "Player " + playerId + " added with code name: " + newCodeName,
                        "Player Added",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    table.setValueAt("", row, 0);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Player ID must be an integer",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
            table.setValueAt("", row, 0);
        }
    }

    private void handleEquipmentIdEntry(JTable table, int row, String teamName) {
        String playerIdStr    = (String) table.getValueAt(row, 0);
        String equipmentIdStr = (String) table.getValueAt(row, 1);
        String codeName       = (String) table.getValueAt(row, 2);

        if (playerIdStr == null || playerIdStr.trim().isEmpty() ||
            equipmentIdStr == null || equipmentIdStr.trim().isEmpty() ||
            codeName == null || codeName.trim().isEmpty()) {
            return;
        }

        try {
            int equipmentId = Integer.parseInt(equipmentIdStr.trim());

            if (usedEquipmentIds.contains(equipmentId)) {
                JOptionPane.showMessageDialog(this,
                    "Equipment ID " + equipmentId + " is already in use!",
                    "Duplicate Equipment ID",
                    JOptionPane.ERROR_MESSAGE);
                table.setValueAt("", row, 1);
                return;
            }

            usedEquipmentIds.add(equipmentId);
            broadcastEquipmentCode(equipmentId);
            updateTeamCounts();

            JOptionPane.showMessageDialog(this,
                "Equipment ID " + equipmentId + " registered for player " + playerIdStr + "\n" +
                "Code Name: " + codeName + "\n" +
                "Team: " + teamName + "\n" +
                "Broadcast sent on port " + BROADCAST_PORT,
                "Player Registered",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Equipment ID must be an integer",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
            table.setValueAt("", row, 1);
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        clearAllButton = new JButton("CLEAR ALL (F12)");
        clearAllButton.setFont(new Font("Arial", Font.BOLD, 16));
        clearAllButton.setBackground(new Color(255, 200, 200));
        clearAllButton.addActionListener(e -> clearAllEntries());
        buttonPanel.add(clearAllButton);

        startButton = new JButton("START GAME (F5)");
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setBackground(new Color(200, 255, 200));
        startButton.addActionListener(e -> startGame());
        buttonPanel.add(startButton);

        JPanel statusPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Team Status"));

        greenCountLabel = new JLabel("Green: 0", SwingConstants.CENTER);
        greenCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        greenCountLabel.setForeground(new Color(74, 163, 117));

        redCountLabel = new JLabel("Red: 0", SwingConstants.CENTER);
        redCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        redCountLabel.setForeground(new Color(204, 75, 86));

        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        statusPanel.add(greenCountLabel);
        statusPanel.add(redCountLabel);
        statusPanel.add(statusLabel);

        buttonPanel.add(statusPanel);

        return buttonPanel;
    }

    private void initDatabase() {
        try {
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://localhost:5432/photon";
            dbConnection = DriverManager.getConnection(url, "student", "student");

            System.out.println("Connected to PostgreSQL database");
            loadPlayersFromDatabase();

        } catch (Exception e) {
            showDatabaseError("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPlayersFromDatabase() throws SQLException {
        String sql = "SELECT id, codename FROM players";
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                playerDatabase.put(rs.getInt("id"), rs.getString("codename"));
            }
        }
        System.out.println("Loaded " + playerDatabase.size() + " players from database");
    }

    private String getCodeNameFromDatabase(int playerId) {
        return playerDatabase.get(playerId);
    }

    private void addPlayerToDatabase(int playerId, String codeName) {
        try {
            String sql = "INSERT INTO players (id, codename) VALUES (?, ?)";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
                pstmt.setInt(1, playerId);
                pstmt.setString(2, codeName);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            try {
                String sql = "UPDATE players SET codename = ? WHERE id = ?";
                try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
                    pstmt.setString(1, codeName);
                    pstmt.setInt(2, playerId);
                    pstmt.executeUpdate();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initUDP() {
        try {
            broadcastSocket = new DatagramSocket();
            broadcastSocket.setBroadcast(true);
            receiveSocket = new DatagramSocket(RECEIVE_PORT, InetAddress.getByName("0.0.0.0"));
            receiveSocket.setSoTimeout(100);
            startUDPReceiver();
            System.out.println(" UDP initialized - Broadcast: " + BROADCAST_PORT + ", Receive: " + RECEIVE_PORT);
        } catch (SocketException | UnknownHostException e) {
            showUDPError("UDP socket error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startUDPReceiver() {
        receiving = true;
        receiveThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (receiving) {
                try {
                    receiveSocket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(" UDP Received: " + received);
                    if (received.contains(":")) {
                        String[] parts = received.split(":");
                        if (parts.length == 2) {
                            int senderId = Integer.parseInt(parts[0].trim());
                            int hitId    = Integer.parseInt(parts[1].trim());
                            System.out.println(" Hit detected: sender=" + senderId + ", hit=" + hitId);
                            broadcastEquipmentCode(hitId);
                        }
                    }
                    buffer = new byte[1024];
                    packet = new DatagramPacket(buffer, buffer.length);
                } catch (SocketTimeoutException e) {
                    // Timeout, continue
                } catch (IOException e) {
                    if (receiving) System.err.println("Receive error: " + e.getMessage());
                }
            }
        });
        receiveThread.start();
    }

    private void broadcastEquipmentCode(int equipmentId) {
        try {
            String message = String.valueOf(equipmentId);
            byte[] buf = message.getBytes();
            InetAddress address = InetAddress.getByName(broadcastIp);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, BROADCAST_PORT);
            broadcastSocket.send(packet);
            System.out.println(" Broadcast equipment ID: " + equipmentId);
            statusLabel.setText("Last broadcast: " + equipmentId);
        } catch (IOException e) {
            System.err.println("Broadcast error: " + e.getMessage());
        }
    }

    private void updateTeamCounts() {
        int greenCount = 0;
        int redCount   = 0;
        // Count green team players
        for (Component comp : greenTeamPanel.getComponents()) 
        {
            if (comp instanceof JScrollPane) 
            {
                JTable table = (JTable) ((JScrollPane) comp).getViewport().getView();

                for (int i = 0; i < table.getRowCount(); i++) 
                {
                    String playerId = (String) table.getValueAt(i, 0);
                    String equipId = (String) table.getValueAt(i, 1);

                    if (playerId != null && equipId != null && !playerId.trim().isEmpty() && !equipId.trim().isEmpty())
                    {
                        greenCount++;
                    }
                    
                }
            }
        }
        // Count red team players
        for (Component comp : redTeamPanel.getComponents()) 
        {
            if (comp instanceof JScrollPane) 
            {
                JTable table = (JTable) ((JScrollPane) comp).getViewport().getView();

                for (int i = 0; i < table.getRowCount(); i++) 
                {
                    String playerId = (String) table.getValueAt(i, 0);
                    String equipId = (String) table.getValueAt(i, 1);

                    if (playerId != null && equipId != null && !playerId.trim().isEmpty() && !equipId.trim().isEmpty())
                    {
                        redCount++;
                    }
                }
                
            }
        }
        greenCountLabel.setText("Green: " + greenCount);
        redCountLabel.setText("Red: " + redCount);
    }

    private void clearAllEntries() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all entries?",
            "Confirm Clear", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Component[] components = {greenTeamPanel, redTeamPanel};
            for (Component panel : components) {
                if (panel instanceof JPanel) {
                    for (Component comp : ((JPanel) panel).getComponents()) {
                        if (comp instanceof JScrollPane) {
                            JViewport viewport = ((JScrollPane) comp).getViewport();
                            if (viewport.getView() instanceof JTable) {
                                JTable table = (JTable) viewport.getView();
                                DefaultTableModel model = (DefaultTableModel) table.getModel();
                                for (int i = 0; i < model.getRowCount(); i++) {
                                    model.setValueAt("", i, 0);
                                    model.setValueAt("", i, 1);
                                    model.setValueAt("", i, 2);
                                }
                            }
                        }
                    }
                }
            }
            usedEquipmentIds.clear();
            statusLabel.setText("All entries cleared");
            updateTeamCounts();
        }
    }

    private void startGame() {
        List<int[]> redPlayers   = new ArrayList<>();
        List<int[]> greenPlayers = new ArrayList<>();
        Map<Integer, String> names = new HashMap<>();

        for (Component comp : greenTeamPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JTable table = (JTable)((JScrollPane)comp).getViewport().getView();
                for (int i = 0; i < table.getRowCount(); i++) {
                    String pid  = (String) table.getValueAt(i, 0);
                    String eid  = (String) table.getValueAt(i, 1);
                    String name = (String) table.getValueAt(i, 2);
                    if (pid != null && eid != null && !pid.trim().isEmpty() && !eid.trim().isEmpty()) {
                        int equipId = Integer.parseInt(eid.trim());
                        greenPlayers.add(new int[]{Integer.parseInt(pid.trim()), equipId, 0, 0});
                        if (name != null && !name.trim().isEmpty()) names.put(equipId, name.trim());
                    }
                }
            }
        }

        for (Component comp : redTeamPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JTable table = (JTable)((JScrollPane)comp).getViewport().getView();
                for (int i = 0; i < table.getRowCount(); i++) {
                    String pid  = (String) table.getValueAt(i, 0);
                    String eid  = (String) table.getValueAt(i, 1);
                    String name = (String) table.getValueAt(i, 2);
                    if (pid != null && eid != null && !pid.trim().isEmpty() && !eid.trim().isEmpty()) {
                        int equipId = Integer.parseInt(eid.trim());
                        redPlayers.add(new int[]{Integer.parseInt(pid.trim()), equipId, 0, 0});
                        if (name != null && !name.trim().isEmpty()) names.put(equipId, name.trim());
                    }
                }
            }
        }

        if (greenPlayers.isEmpty() || redPlayers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Need at least 1 player on each team!",
                "Cannot Start", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show 30 second warning
        int warning = JOptionPane.showConfirmDialog(this,
            "Game will start in 30 seconds.\nPrepare players!\n\nClick OK to continue.",
            "Game Starting Soon",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (warning == JOptionPane.OK_OPTION) 
        {
            // This is where countdown.java should start running
            new countdown();

            // Broadcast game start code 202
            broadcastCode(202);

            // Here you would open the Play Action Screen
            JOptionPane.showMessageDialog(this,
                "GAME STARTED!\n\nBroadcast code 202 sent on port " + BROADCAST_PORT +
                "Game Started",
                JOptionPane.INFORMATION_MESSAGE);
            
            // For now, just update status
            statusLabel.setText("Game in progress...");

            dispose();
            new PlayActionDisplay(redPlayers, greenPlayers, names);
        }

    }

    private void broadcastCode(int code) {
        try {
            String message = String.valueOf(code);
            byte[] buf = message.getBytes();
            InetAddress address = InetAddress.getByName(broadcastIp);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, BROADCAST_PORT);
            broadcastSocket.send(packet);
            System.out.println(" Broadcast code: " + code);
        } catch (IOException e) {
            System.err.println("Broadcast error: " + e.getMessage());
        }
    }

    private void showSettingsDialog() {
        String newIp = JOptionPane.showInputDialog(this,
            "Enter broadcast IP address:", broadcastIp);
        if (newIp != null && !newIp.trim().isEmpty()) {
            if (newIp.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                broadcastIp = newIp;
                JOptionPane.showMessageDialog(this, "Broadcast IP changed to: " + broadcastIp);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid IP address format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDatabaseError(String message) {
        JOptionPane.showMessageDialog(this,
            message + "\n\nMake sure PostgreSQL is running and configured correctly.",
            "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showUDPError(String message) {
        JOptionPane.showMessageDialog(this, message, "UDP Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addKeyBindings() {
        JRootPane rootPane = getRootPane();

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F12"), "clearAll");
        rootPane.getActionMap().put("clearAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { clearAllEntries(); }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "startGame");
        rootPane.getActionMap().put("startGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { startGame(); }
        });
    }

    @Override
    public void dispose() {
        receiving = false;
        if (receiveThread != null) receiveThread.interrupt();
        if (broadcastSocket != null && !broadcastSocket.isClosed()) broadcastSocket.close();
        if (receiveSocket  != null && !receiveSocket.isClosed())  receiveSocket.close();
        try {
            if (dbConnection != null && !dbConnection.isClosed()) dbConnection.close();
        } catch (SQLException e) { e.printStackTrace(); }
        super.dispose();
    }
}
