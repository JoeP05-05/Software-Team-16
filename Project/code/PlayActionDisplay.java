//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
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

    private List<int[]> redPlayers;
    private List<int[]> greenPlayers;
    private Map<Integer, String> playerNames;
    private Map<Integer, Integer> playerScores;

    private int countdownSeconds = 30;
    private int gameSeconds      = 360;
    private boolean gameRunning  = false;
    private javax.swing.Timer gameTimer;

    private JLabel    timerLabel;
    private JLabel    redTotalLabel;
    private JLabel    greenTotalLabel;
    private JPanel    redListPanel;
    private JPanel    greenListPanel;
    private JTextArea eventLog;
    private JButton   returnButton;

    private DatagramSocket udpSocket;
    private String networkAddress = "127.0.0.1";

    public PlayActionDisplay(List<int[]> redPlayers, List<int[]> greenPlayers, Map<Integer, String> playerNames) {
        this.redPlayers   = redPlayers;
        this.greenPlayers = greenPlayers;
        this.playerNames  = playerNames;

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
        startPreGameCountdown();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(BG_BLACK);
        outer.setBorder(BorderFactory.createLineBorder(BORDER_COL, 3));
        add(outer, BorderLayout.CENTER);

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

    private void refreshPlayerPanels() {
        SwingUtilities.invokeLater(() -> {
            populateList(redListPanel,   redPlayers,   RED_COL);
            populateList(greenListPanel, greenPlayers, GREEN_COL);
            redTotalLabel.setText("0");
            greenTotalLabel.setText("0");
        });
    }

    private void populateList(JPanel panel, List<int[]> players, Color color) {
        panel.removeAll();

        for (int[] p : players) {
            int    equipId = p[1];
            String name    = playerNames.getOrDefault(equipId, "Player " + equipId);

            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(BG_BLACK);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setBorder(new EmptyBorder(2, 10, 2, 10));

            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 16));
            nameLbl.setForeground(color);

            JLabel scoreLbl = new JLabel("0", SwingConstants.RIGHT);
            scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
            scoreLbl.setForeground(color);

            row.add(nameLbl,  BorderLayout.WEST);
            row.add(scoreLbl, BorderLayout.EAST);
            panel.add(row);
        }

        panel.revalidate();
        panel.repaint();
    }

    private void startPreGameCountdown() {
        updateTimerLabel(countdownSeconds);

        gameTimer = new javax.swing.Timer(1000, e -> {
            countdownSeconds--;
            updateTimerLabel(countdownSeconds);
            if (countdownSeconds <= 0) {
                gameTimer.stop();
                broadcastCode(202);
                startGameTimer();
            }
        });
        gameTimer.start();
    }

    private void startGameTimer() {
        gameRunning = true;

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


    //Tagging method
    private void playerHit(String data)
    {
        try{
            String[] split = data.split(":");
            //For the person who tagged someone
            int taggerID = Integer.parseInt(split[0]);

            //for the person who got tagged
            int targetID = Integer.parseInt(split[1]);

            boolean isTaggerRed = redPlayers.stream().anyMatch(p -> p[0] == taggerID);
            boolean isTargetRed = redPlayers.stream().anyMatch(p -> p[0] == targetID);
            boolean friendlyFire = (isTaggerRed && isTargetRed) || (!isTaggerRed && !isTargetRed);

            //if there is freindly fire, it will broadcast their ids
            if (friendlyFire)
            {
                broadcastCode(taggerID);
                broadcastCode(targetID);
            }
            //broadcast normally
            else
            {
                broadcastCode(targetID);
            }
        } catch (Exception e) {
            System.err.println("Error on hit: " + e.getMessage());

        }
    }


    private void endGame() {
        gameRunning = false;
        timerLabel.setText("GAME OVER");
        timerLabel.setForeground(Color.RED);
        for (int i = 0; i < 3; i++) broadcastCode(221);
        returnButton.setVisible(true);
    }

    private void initUDP() {
        try {
            udpSocket = new DatagramSocket();
            
            //Listens on port 7501
            DatagramSocket receiveCodes = new DatagramSocket(7501);
            receiveCodes.setBroadcast(true);
            new Thread(() -> {
                byte[] bufferData = new byte[1024];
                while (true)
                {
                    try{
                        DatagramPacket playerPacket = new DatagramPacket(bufferData, bufferData.length);
                        receiveCodes.receive(playerPacket);
                        String data = new String(playerPacket.getData(), 0, playerPacket.getLength()).trim();
                        playerHit(data);

                    } catch (Exception e){
                        System.err.println("Error on receive: " + e.getMessage());
                    }
                }

            }).start();



        } catch (Exception ex) {
            System.err.println("UDP error: " + ex.getMessage());
        }
    }

    private void broadcastCode(int code) {
        try {
            byte[] data = String.valueOf(code).getBytes();
            DatagramPacket pkt = new DatagramPacket(
                data, data.length, InetAddress.getByName(networkAddress), 7500);
            udpSocket.send(pkt);
        } catch (Exception ex) {
            System.err.println("UDP send error: " + ex.getMessage());
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
        if (gameTimer != null) gameTimer.stop();
        if (udpSocket != null && !udpSocket.isClosed()) udpSocket.close();
        dispose();
        new Player_Entry();
    }
}
