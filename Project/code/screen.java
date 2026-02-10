
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import java.io.InputStream;
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
        int width = 1000;
        int height = 700;
       
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
       
        setSize(width, height);
        setLocation(x, y);
    }
   
    private void loadImage() {
        try {
            // Try multiple methods to load the image
            File file = new File("logo.jpg");
           
            if (file.exists()) {
                // Method 1: From file in current directory
                System.out.println("Loading logo.jpg from current directory");
                backgroundImage = ImageIO.read(file);
            } else {
                // Method 2: From classpath (if packaged in JAR)
                System.out.println("Trying to load from classpath...");
                InputStream is = getClass().getClassLoader().getResourceAsStream("logo.jpg");
                if (is != null) {
                    backgroundImage = ImageIO.read(is);
                } else {
                    // Method 3: Create a simple colored background
                    System.out.println("Creating fallback background");
                    backgroundImage = createFallbackImage();
                }
            }
           
            if (backgroundImage != null) {
                backgroundImage = backgroundImage.getScaledInstance(
                    getWidth(), getHeight(), Image.SCALE_SMOOTH);
            }
           
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            backgroundImage = createFallbackImage();
        }
    }
   
    private Image createFallbackImage() {
        // Create a simple gradient background
        BufferedImage img = new BufferedImage(
            getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
       
        // Create gradient
        GradientPaint gradient = new GradientPaint(
            0, 0, Color.BLACK,
            getWidth(), getHeight(), Color.DARK_GRAY);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
       
        // Add text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        String text = "Splash Screen";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (getWidth() - textWidth) / 2, getHeight() / 2);
       
        g2d.dispose();
        return img;
    }
   
    private void setupUI() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
               
                // Draw background image
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, this);
                }
               
                // Draw black frame (like in your original Python code)
                g.setColor(Color.BLACK);
                g.fillRect(50, 100, 427, 241);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(50, 100, 427, 241);
            }
        };
       
        setContentPane(mainPanel);
        setVisible(true);
    }
   
  //Remove the Timer HERE!!
    private void startTimer() {
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
   
    public static void main(String[] args) {
        // Set look and feel to system default for better appearance
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
