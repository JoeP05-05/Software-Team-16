import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Player_Entry extends JFrame {

    private static final int PLAYER_COUNT = 15;

    private JTextField[][] greenFields = new JTextField[PLAYER_COUNT][3];
    private JTextField[][] redFields = new JTextField[PLAYER_COUNT][3];

    private Set<Integer> usedEquipmentIds = new HashSet<>();
    private Set<Integer> usedUserIds = new HashSet<>();

    public Player_Entry() {
        setTitle("Team Entry");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(createTeamPanel("Green Team", new Color(74, 163, 117)));
        mainPanel.add(createTeamPanel("Red Team", new Color(204, 75, 86)));

        add(mainPanel);

        addKeyBindings();

        setVisible(true);
    }

    private JPanel createTeamPanel(String teamName, Color bgColor) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel title = new JLabel(teamName, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        panel.add(createHeader("Equipment ID"), headerGbc(gbc, 0));
        panel.add(createHeader("User ID"), headerGbc(gbc, 1));
        panel.add(createHeader("Username"), headerGbc(gbc, 2));

        for (int i = 0; i < PLAYER_COUNT; i++) {
            gbc.gridy = i + 2;

            JTextField equipmentField = new JTextField(10);
            JTextField userField = new JTextField(10);
            JTextField usernameField = new JTextField(12);

            if (teamName.contains("Green")) {
                greenFields[i][0] = equipmentField;
                greenFields[i][1] = userField;
                greenFields[i][2] = usernameField;
            } else {
                redFields[i][0] = equipmentField;
                redFields[i][1] = userField;
                redFields[i][2] = usernameField;
            }

            addValidation(equipmentField, "equipment");
            addValidation(userField, "user");

            gbc.gridx = 0;
            panel.add(equipmentField, gbc);

            gbc.gridx = 1;
            panel.add(userField, gbc);

            gbc.gridx = 2;
            panel.add(usernameField, gbc);
        }

        return panel;
    }

    private void addValidation(JTextField field, String type) {
        field.addActionListener(e -> {
            String text = field.getText();

            if (!text.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        type.equals("equipment") ?
                                "Equipment ID must be an integer" :
                                "User ID must be an integer");
                field.setText("");
                field.requestFocus();
                return;
            }

            int value = Integer.parseInt(text);

            if (type.equals("equipment")) {

                if (value < 0 || value > 100) {
                    JOptionPane.showMessageDialog(this,
                            "Equipment ID must be between 0 and 100");
                    field.setText("");
                    field.requestFocus();
                    return;
                }

                if (usedEquipmentIds.contains(value)) {
                    JOptionPane.showMessageDialog(this,
                            "Equipment ID already used");
                    field.setText("");
                    field.requestFocus();
                    return;
                }

                usedEquipmentIds.add(value);
            }

            if (type.equals("user")) {
                if (usedUserIds.contains(value)) {
                    JOptionPane.showMessageDialog(this,
                            "User ID already used");
                    field.setText("");
                    field.requestFocus();
                    return;
                }

                usedUserIds.add(value);
            }
        });
    }

    private JLabel createHeader(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        return label;
    }

    private GridBagConstraints headerGbc(GridBagConstraints base, int x) {
        GridBagConstraints gbc = (GridBagConstraints) base.clone();
        gbc.gridx = x;
        return gbc;
    }

    private void addKeyBindings() {
        JRootPane rootPane = getRootPane();

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F12"), "reset");

        rootPane.getActionMap().put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                resetFields();
            }
        });

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("F5"), "start");

        rootPane.getActionMap().put("start", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startGame();
            }
        });
    }

    private void resetFields() {
        usedEquipmentIds.clear();
        usedUserIds.clear();

        for (int i = 0; i < PLAYER_COUNT; i++) {
            for (int j = 0; j < 3; j++) {
                greenFields[i][j].setText("");
                redFields[i][j].setText("");
            }
        }

        greenFields[0][0].requestFocus();
    }

    private void startGame() {
        int greenCount = 0;
        int redCount = 0;

        for (int i = 0; i < PLAYER_COUNT; i++) {
            if (!greenFields[i][0].getText().isEmpty())
                greenCount++;
            if (!redFields[i][0].getText().isEmpty())
                redCount++;
        }

        if (greenCount < 1 || redCount < 1) {
            JOptionPane.showMessageDialog(this,
                    "There must be at least 1 user on each team");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Game Starting...");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Player_Entry::new);
    }
}
