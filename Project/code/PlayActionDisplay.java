//Names: Joss Jongewaard, Taija Frierson, Kaija Frierson, Joseph Peraza
//Team: Team 16
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.net.*;

public class PlayActionDisplay extends JFrame {

    private static final Color BG_BLACK   = Color.BLACK;
    private static final Color BORDER_COL = new Color(180, 220, 0);
    private static final Color CYAN_LABEL = new Color(0, 220, 220);
    private static final Color RED_COL    = new Color(255, 80, 80);
    private static final Color GREEN_COL  = new Color(0, 220, 120);
    private static final Color EVENT_BG   = new Color(30, 30, 180);

    private static final int    SEND_PORT    = 7500;
    private static final int    RECEIVE_PORT = 7501;
    private static final String BASE_ICON_PATH =
        "baseicon.jpg";

    private List<int[]>          redPlayers;
    private List<int[]>          greenPlayers;
    private Map<Integer, String> playerNames;

    /** equipmentId → current score */
    private final Map<Integer, Integer> playerScores = new HashMap<>();

    /** Fast team-membership lookup */
    private final Set<Integer> redEquipIds   = new HashSet<>();
    private final Set<Integer> greenEquipIds = new HashSet<>();

    /** equipmentId → whether the base icon has been awarded */
    private final Set<Integer> hasBaseIcon = new HashSet<>();

    private int  gameSeconds      = 360;
    private boolean gameRunning   = false;
    private javax.swing.Timer gameTimer;
    private javax.swing.Timer flashTimer;
    private boolean flashState = false;


    private JLabel    timerLabel;
    private JLabel    redTotalLabel;
    private JLabel    greenTotalLabel;
    private JPanel    redListPanel;
    private JPanel    greenListPanel;
    private JTextArea eventLog;
    private JButton   returnButton;


    private DatagramSocket udpSendSocket;
    private DatagramSocket udpReceiveSocket;
    private String networkAddress = "127.0.0.1";


    private ImageIcon baseIcon;


    public PlayActionDisplay(List<int[]> redPlayers,
                             List<int[]> greenPlayers,
                             Map<Integer, String> playerNames) {
        this.redPlayers   = redPlayers;
        this.greenPlayers = greenPlayers;
        this.playerNames  = playerNames;

        // Initialise scores to 0 and populate team-id sets
        for (int[] p : redPlayers) {
            playerScores.put(p[1], 0);
            redEquipIds.add(p[1]);
        }
        for (int[] p : greenPlayers) {
            playerScores.put(p[1], 0);
            greenEquipIds.add(p[1]);
        }

        loadBaseIcon();

        setTitle("Photon Laser Tag");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_BLACK);

        buildUI();
        addKeyBindings();
        initUDP();
        setVisible(true);
        startGameTimer();
    }

    private void loadBaseIcon() {
        try {
            ImageIcon raw = new ImageIcon(BASE_ICON_PATH);
            Image scaled  = raw.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            baseIcon = new ImageIcon(scaled);
        } catch (Exception e) {
            baseIcon = null; // graceful fallback — icon simply won't appear
            System.err.println("Could not load base icon: " + e.getMessage());
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(BG_BLACK);
        outer.setBorder(BorderFactory.createLineBorder(BORDER_COL, 3));
        add(outer, BorderLayout.CENTER);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_BLACK);
        header.setBorder(new EmptyBorder(4, 8, 2, 8));

        JLabel xpLabel = new JLabel("XP");
        xpLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        xpLabel.setForeground(new Color(255, 140, 0));
        header.add(xpLabel, BorderLayout.WEST);

        JLabel scoresTitle = new JLabel("Current Scores", SwingConstants.RIGHT);
        scoresTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoresTitle.setForeground(CYAN_LABEL);
        header.add(scoresTitle, BorderLayout.EAST);

        outer.add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 0));
        mainContent.setBackground(BG_BLACK);
        outer.add(mainContent, BorderLayout.CENTER);

        JPanel scoresArea = new JPanel(new GridLayout(1, 2, 2, 0));
        scoresArea.setBackground(BG_BLACK);
        scoresArea.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));
        scoresArea.setPreferredSize(new Dimension(0, 310));
        scoresArea.add(buildTeamColumn("RED TEAM",   RED_COL,   true));
        scoresArea.add(buildTeamColumn("GREEN TEAM", GREEN_COL, false));
        mainContent.add(scoresArea, BorderLayout.NORTH);

        //  Event log 
        JPanel eventArea = new JPanel(new BorderLayout(0, 0));
        eventArea.setBackground(BG_BLACK);
        eventArea.setBorder(BorderFactory.createLineBorder(BORDER_COL, 1));

        JLabel eventTitle = new JLabel("Current Game Action  ", SwingConstants.RIGHT);
        eventTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        eventTitle.setForeground(CYAN_LABEL);
        eventTitle.setOpaque(true);
        eventTitle.setBackground(BG_BLACK);
        eventTitle.setBorder(new EmptyBorder(3, 0, 3, 8));
        eventArea.add(eventTitle, BorderLayout.NORTH);

        eventLog = new JTextArea();
        eventLog.setEditable(false);
        eventLog.setBackground(EVENT_BG);
        eventLog.setForeground(Color.WHITE);
        eventLog.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 15));
        eventLog.setLineWrap(true);
        eventLog.setWrapStyleWord(true);
        eventLog.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane eventScroll = new JScrollPane(eventLog);
        eventScroll.setBorder(null);
        eventArea.add(eventScroll, BorderLayout.CENTER);
        mainContent.add(eventArea, BorderLayout.CENTER);

        // Timer bar
        JPanel timerBar = new JPanel(new BorderLayout());
        timerBar.setBackground(BG_BLACK);
        timerBar.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_COL));

        timerLabel = new JLabel("Time Remaining:  0:30", SwingConstants.CENTER);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBorder(new EmptyBorder(6, 0, 6, 0));
        timerBar.add(timerLabel, BorderLayout.CENTER);

        returnButton = new JButton("Return to Player Entry  [F12]");
        returnButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        returnButton.setBackground(new Color(60, 60, 60));
        returnButton.setForeground(Color.WHITE);
        returnButton.setFocusPainted(false);
        returnButton.setVisible(false);
        returnButton.addActionListener(e -> returnToPlayerEntry());
        timerBar.add(returnButton, BorderLayout.EAST);

        outer.add(timerBar, BorderLayout.SOUTH);

        refreshPlayerPanels();
    }

    private JPanel buildTeamColumn(String title, Color teamColor, boolean isRed) {
        JPanel col = new JPanel(new BorderLayout());
        col.setBackground(BG_BLACK);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setBorder(new EmptyBorder(6, 0, 6, 0));
        col.add(titleLbl, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG_BLACK);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.setBackground(BG_BLACK);
        scroll.getViewport().setBackground(BG_BLACK);
        col.add(scroll, BorderLayout.CENTER);

        if (isRed) redListPanel   = listPanel;
        else       greenListPanel = listPanel;

        JLabel totalLbl = new JLabel("0", SwingConstants.RIGHT);
        totalLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        totalLbl.setForeground(teamColor);
        totalLbl.setBorder(new EmptyBorder(4, 8, 4, 16));
        col.add(totalLbl, BorderLayout.SOUTH);

        if (isRed) redTotalLabel   = totalLbl;
        else       greenTotalLabel = totalLbl;

        return col;
    }

    /** Full initial population (scores all 0). */
    private void refreshPlayerPanels() {
        SwingUtilities.invokeLater(() -> {
            populateSortedList(redListPanel,   redPlayers,   RED_COL);
            populateSortedList(greenListPanel, greenPlayers, GREEN_COL);
            redTotalLabel.setText("0");
            greenTotalLabel.setText("0");
        });
    }

    /**
     * Re-render a team's list sorted by current score, highest first.
     * Called after every scoring event.
     */
    private void populateSortedList(JPanel panel, List<int[]> players, Color color) {
        // Sort a copy so the original list order is unaffected
        List<int[]> sorted = new ArrayList<>(players);
        sorted.sort((a, b) ->
            playerScores.getOrDefault(b[1], 0) - playerScores.getOrDefault(a[1], 0));

        panel.removeAll();

        for (int[] p : sorted) {
            int    equipId = p[1];
            String name    = playerNames.getOrDefault(equipId, "Player " + equipId);
            int    score   = playerScores.getOrDefault(equipId, 0);

            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(BG_BLACK);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setBorder(new EmptyBorder(2, 10, 2, 10));

            // Left side: optional base icon + player name
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            namePanel.setBackground(BG_BLACK);

            if (hasBaseIcon.contains(equipId) && baseIcon != null) {
                namePanel.add(new JLabel(baseIcon));
            }

            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
            nameLbl.setForeground(color);
            namePanel.add(nameLbl);

            JLabel scoreLbl = new JLabel(String.valueOf(score), SwingConstants.RIGHT);
            scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            scoreLbl.setForeground(color);

            row.add(namePanel, BorderLayout.WEST);
            row.add(scoreLbl, BorderLayout.EAST);
            panel.add(row);
        }

        panel.revalidate();
        panel.repaint();
    }


    private void adjustScore(int equipId, int delta) {
        playerScores.merge(equipId, delta, Integer::sum);
    }

    private int teamTotal(Set<Integer> teamIds) {
        return teamIds.stream().mapToInt(id -> playerScores.getOrDefault(id, 0)).sum();
    }

    /**
     * Recompute both team totals and update labels.
     * Also updates the flash timer to flash whichever team is currently winning.
     */
    private void updateTeamTotals() {
        int redTotal   = teamTotal(redEquipIds);
        int greenTotal = teamTotal(greenEquipIds);
        redTotalLabel.setText(String.valueOf(redTotal));
        greenTotalLabel.setText(String.valueOf(greenTotal));
        updateFlashTarget(redTotal, greenTotal);
    }

    /** Award the base icon to a player (idempotent). */
    private void awardBaseIcon(int equipId) {
        hasBaseIcon.add(equipId);
    }

    private void logEvent(String message) {
        SwingUtilities.invokeLater(() -> {
            eventLog.append(message + "\n");
            // Auto-scroll to bottom
            eventLog.setCaretPosition(eventLog.getDocument().getLength());
        });
    }

    /**
     * Called whenever a UDP hit packet is received.
     * Packet format assumed: "attackerEquipId:targetEquipId"
     *
     * Special target codes:
     *   53 → red base was hit
     *   43 → green base was hit
     */
    private void processHitEvent(int attackerId, int targetId) {
        if (!gameRunning) return;

        String attackerName = playerNames.getOrDefault(attackerId, "Player " + attackerId);

        //Base hit: code 53 → red base scored 
        if (targetId == 53) {
            if (greenEquipIds.contains(attackerId)) {
                adjustScore(attackerId, 100);
                awardBaseIcon(attackerId);
                logEvent("[BASE] " + attackerName + " scored on the RED BASE! +100 pts");
                refreshAndUpdate();
            }
            return;
        }

        // Base hit: code 43 → green base scored
        if (targetId == 43) {
            if (redEquipIds.contains(attackerId)) {
                adjustScore(attackerId, 100);
                awardBaseIcon(attackerId);
                logEvent("[BASE] " + attackerName + " scored on the GREEN BASE! +100 pts");
                refreshAndUpdate();
            }
            return;
        }

        // Player vs. player
        String targetName = playerNames.getOrDefault(targetId, "Player " + targetId);

        boolean attackerRed   = redEquipIds.contains(attackerId);
        boolean attackerGreen = greenEquipIds.contains(attackerId);
        boolean targetRed     = redEquipIds.contains(targetId);
        boolean targetGreen   = greenEquipIds.contains(targetId);

        // Always broadcast the equipment ID of the player that was hit
        broadcastCode(targetId);

        if ((attackerRed && targetRed) || (attackerGreen && targetGreen)) {
            // Friendly fire
            // Also broadcast the attacker's own equipment ID (two transmissions total)
            broadcastCode(attackerId);

            adjustScore(attackerId, -10);
            adjustScore(targetId,   -10);
            logEvent("[FF] " + attackerName + " hit teammate " + targetName
                     + "! Both lose 10 pts");
        } else if ((attackerRed && targetGreen) || (attackerGreen && targetRed)) {
            // Enemy tag
            adjustScore(attackerId, 10);
            logEvent("[HIT] " + attackerName + " tagged " + targetName + "! +10 pts");
        }
        // (Unknown IDs are silently ignored)

        refreshAndUpdate();
    }

    /** Convenience: rebuild sorted panels + update totals on the EDT. */
    private void refreshAndUpdate() {
        SwingUtilities.invokeLater(() -> {
            populateSortedList(redListPanel,   redPlayers,   RED_COL);
            populateSortedList(greenListPanel, greenPlayers, GREEN_COL);
            updateTeamTotals();
        });
    }


    private JLabel flashingLabel = null; // whichever total label is currently flashing

    private void startFlashTimer() {
        flashTimer = new javax.swing.Timer(500, e -> {
            if (flashingLabel != null) {
                flashState = !flashState;
                flashingLabel.setOpaque(flashState);
                flashingLabel.setBackground(flashState ? Color.WHITE : BG_BLACK);
                flashingLabel.repaint();
            }
        });
        flashTimer.start();
    }

    /**
     * Ensure the correct (leading) team total label flashes.
     * Tied → neither flashes.
     */
    private void updateFlashTarget(int redTotal, int greenTotal) {
        // Stop flashing both first
        if (flashingLabel != null) {
            flashingLabel.setOpaque(false);
            flashingLabel.repaint();
        }
        if (redTotal > greenTotal) {
            flashingLabel = redTotalLabel;
        } else if (greenTotal > redTotal) {
            flashingLabel = greenTotalLabel;
        } else {
            flashingLabel = null;
        }
    }


    private void startGameTimer() {
        broadcastCode(202);
        gameRunning = true;
        startFlashTimer();

        gameTimer = new javax.swing.Timer(1000, e -> {
            gameSeconds--;
            updateTimerLabel(gameSeconds);
            if (gameSeconds <= 0) {
                gameTimer.stop();
                endGame();
            }
        });
        gameTimer.start();
    }

    private void updateTimerLabel(int secs) {
        int m = secs / 60, s = secs % 60;
        timerLabel.setText("Time Remaining:  " + m + ":" + String.format("%02d", s));
    }

    private void endGame() {
        gameRunning = false;

        // Stop flash timer
        if (flashTimer != null) flashTimer.stop();
        if (flashingLabel != null) {
            flashingLabel.setOpaque(false);
            flashingLabel.repaint();
        }

        timerLabel.setText("GAME OVER");
        timerLabel.setForeground(Color.RED);

        // Broadcast code 221 three times
        for (int i = 0; i < 3; i++) broadcastCode(221);

        logEvent("=== GAME OVER ===");
        logEvent("Red Team:   " + teamTotal(redEquipIds) + " pts");
        logEvent("Green Team: " + teamTotal(greenEquipIds) + " pts");

        returnButton.setVisible(true);
    }


    private void initUDP() {
        try {
            udpSendSocket = new DatagramSocket();
        } catch (Exception ex) {
            System.err.println("UDP send socket error: " + ex.getMessage());
        }
        startUDPReceiver();
    }

    private void broadcastCode(int code) {
        try {
            byte[] data = String.valueOf(code).getBytes();
            DatagramPacket pkt = new DatagramPacket(
                data, data.length,
                InetAddress.getByName(networkAddress), SEND_PORT);
            udpSendSocket.send(pkt);
        } catch (Exception ex) {
            System.err.println("UDP send error: " + ex.getMessage());
        }
    }

    /**
     * Starts a daemon thread that listens for incoming UDP packets on
     * RECEIVE_PORT. Expected packet format: "attackerEquipId:targetEquipId"
     */
    private void startUDPReceiver() {
        Thread receiver = new Thread(() -> {
            try {
                udpReceiveSocket = new DatagramSocket(RECEIVE_PORT);
                byte[] buf = new byte[256];

                while (!udpReceiveSocket.isClosed()) {
                    DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                    udpReceiveSocket.receive(pkt);

                    String msg = new String(pkt.getData(), 0, pkt.getLength()).trim();
                    parseAndProcess(msg);
                }
            } catch (Exception ex) {
                if (!udpReceiveSocket.isClosed()) {
                    System.err.println("UDP receive error: " + ex.getMessage());
                }
            }
        }, "UDP-Receiver");
        receiver.setDaemon(true);
        receiver.start();
    }

    /**
     * Parse a UDP message of the form "attackerId:targetId" and dispatch to
     * {@link #processHitEvent(int, int)}.
     */
    private void parseAndProcess(String msg) {
        try {
            String[] parts = msg.split(":");
            if (parts.length == 2) {
                int attackerId = Integer.parseInt(parts[0].trim());
                int targetId   = Integer.parseInt(parts[1].trim());
                processHitEvent(attackerId, targetId);
            } else {
                System.err.println("Unexpected UDP message format: " + msg);
            }
        } catch (NumberFormatException ex) {
            System.err.println("Could not parse UDP message: " + msg);
        }
    }

    private void addKeyBindings() {
        JRootPane rootPane = getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F12"), "returnToEntry");
        rootPane.getActionMap().put("returnToEntry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { returnToPlayerEntry(); }
        });
    }

    private void returnToPlayerEntry() {
        if (gameTimer  != null) gameTimer.stop();
        if (flashTimer != null) flashTimer.stop();
        if (udpSendSocket    != null && !udpSendSocket.isClosed())    udpSendSocket.close();
        if (udpReceiveSocket != null && !udpReceiveSocket.isClosed()) udpReceiveSocket.close();
        dispose();
        new Player_Entry();
    }
}
