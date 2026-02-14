import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class screen extends JWindow {
   
    private Image backgroundImage;
   
    public screen() {
        setupWindow();
        loadImage();
        setupUI();
        startTimer();
    }
   
    private void setupWindow() {
        // First load image to get dimensions
        loadImageDimensions();
        
        // Center the window on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        
        setLocation(x, y);
    }
   
    private void loadImageDimensions() {
        try {
            File file = new File("logo.jpg");
            if (file.exists()) {
                Image img = ImageIO.read(file);
                // Set window size to match image
                setSize(img.getWidth(null), img.getHeight(null));
            } else {
                // Fallback size if image not found
                setSize(800, 600);
            }
        } catch (Exception e) {
            setSize(800, 600);
        }
    }
   
    private void loadImage() {
        try {
            File file = new File("logo.jpg");
           
            if (file.exists()) {
                System.out.println("Loading logo.jpg from current directory");
                // Load the image at its original size
                backgroundImage = ImageIO.read(file);
                System.out.println("Image loaded: " + backgroundImage.getWidth(null) + "x" + backgroundImage.getHeight(null));
            } else {
                System.out.println("ERROR: logo.jpg not found in current directory!");
                System.out.println("Current directory: " + System.getProperty("user.dir"));
                // Create a simple error background
                backgroundImage = createErrorImage();
            }
           
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            backgroundImage = createErrorImage();
        }
    }
   
    private Image createErrorImage() {
        // Create a simple error message image if logo.jpg is missing
        BufferedImage img = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
       
        // Red background
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 600, 400);
       
        // White text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String text = "logo.jpg not found!";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (600 - textWidth) / 2, 200);
        
        // Add current directory info
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        text = "Current dir: " + System.getProperty("user.dir");
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(text);
        g2d.drawString(text, (600 - textWidth) / 2, 250);
       
        g2d.dispose();
        return img;
    }
   
    private void setupUI() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
               
                // Draw ONLY the background image, nothing else
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this);
                }
            }
        };
        
        // Remove any borders, layouts, etc.
        mainPanel.setLayout(null);
        
        setContentPane(mainPanel);
        setVisible(true);
    }
   
    private void startTimer() {
        // Show splash for 3 seconds then open player entry
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                // Open player entry after splash
                SwingUtilities.invokeLater(() -> new Player_Entry());
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
   
    // ADD THIS MAIN METHOD
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Run the splash screen on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new screen();
            }
        });
    }
}
