import javax.swing.*;
import java.awt.*;

public class Player_Entry extends JFrame {

    private static final int PLAYER_COUNT = 15;

    public Player_Entry() {
        setTitle("Team Entry");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(createTeamPanel("Green Team", new Color(74, 163, 117)));
        mainPanel.add(createTeamPanel("Red Team", new Color(204, 75, 86)));

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createTeamPanel(String teamName, Color bgColor) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Team title
        JLabel title = new JLabel(teamName, SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        panel.add(title, gbc);

        // Column headers
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        panel.add(createHeader("Equipment ID"), headerGbc(gbc, 0));
        panel.add(createHeader("User ID"), headerGbc(gbc, 1));
        panel.add(createHeader("Username"), headerGbc(gbc, 2));

        // Player rows
        for (int i = 0; i < PLAYER_COUNT; i++) {
            gbc.gridy = i + 2;

            gbc.gridx = 0;
            panel.add(new JTextField(10), gbc);

            gbc.gridx = 1;
            panel.add(new JTextField(10), gbc);

            gbc.gridx = 2;
            panel.add(new JTextField(12), gbc);
        }

        return panel;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Player_Entry::new);
    }
}
