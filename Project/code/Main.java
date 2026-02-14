import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {

    private static JFrame root;

    public static void main(String[] args) {

        // Initialize PostgreSQL database
        Database.initialize();

        // Build window
        buildRoot();

        // Show splash screen (Splash.build adds the label to the frame)
        JLabel splash = Splash.build(root);
        root.revalidate();
        root.repaint();

        // After 3 seconds, switch screen
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            root.getContentPane().remove(splash);
            root.revalidate();
            root.repaint();
        });

        timer.setRepeats(false);
        timer.start();

        root.setVisible(true);
    }

    private static void buildRoot() {

        root = new JFrame("Photon");
        root.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        root.setExtendedState(JFrame.MAXIMIZED_BOTH);
        root.setLayout(new BorderLayout());
        root.getContentPane().setBackground(Color.WHITE);
        root.setResizable(false);

        root.getRootPane().registerKeyboardAction(
                e -> destroyRoot(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        root.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroyRoot();
            }
        });
    }

    private static void destroyRoot() {
        root.dispose();
        System.exit(0);
    }
}
