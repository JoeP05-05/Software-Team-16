//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
//Team: Team 16 
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Show splash screen first, which will open player entry after 3 seconds
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new screen();
            }
        });
    }
}
