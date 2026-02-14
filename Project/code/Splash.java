import javax.swing.*;
import java.awt.*;

public class Splash {

    // Build the splash screen, destroy after 3 seconds (timing handled elsewhere)
    public static JLabel build(JFrame frame) {
        // Load the splash screen image
        ImageIcon splashImage = new ImageIcon("assets/images/splash.jpg");

        // Create the splash screen label
        JLabel splashScreen = new JLabel(splashImage);
        splashScreen.setHorizontalAlignment(JLabel.CENTER);
        splashScreen.setVerticalAlignment(JLabel.CENTER);

        // Make background black
        splashScreen.setOpaque(true);
        splashScreen.setBackground(Color.BLACK);

        // Make it fill the frame
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(splashScreen, BorderLayout.CENTER);

        return splashScreen;
    }
}
