//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
//Team: Team 16 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;

public class screen extends JWindow {
   
    private Image backgroundImage;
    private BufferedImage originalImage;
   
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
                // Read the original image once and save it
                originalImage = ImageIO.read(file);
                int imgW = originalImage.getWidth();
                int imgH = originalImage.getHeight();

                // Compute max allowed size (90% of screen) so it fits most displays
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int maxW = (int) (screenSize.width * 0.9);
                int maxH = (int) (screenSize.height * 0.9);

                double scale = Math.min(1.0, Math.min((double) maxW / imgW, (double) maxH / imgH));
                int newW = (int) Math.round(imgW * scale);
                int newH = (int) Math.round(imgH * scale);

                // Set window size to scaled image size
                setSize(newW, newH);
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
                // Use the already-read original image if available
                if (originalImage == null) {
                    originalImage = ImageIO.read(file);
                }

                // Scale the image to the window size while preserving aspect ratio
                int targetW = getWidth();
                int targetH = getHeight();

                BufferedImage scaled = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.drawImage(originalImage, 0, 0, targetW, targetH, null);
                g2.dispose();

                backgroundImage = scaled;
                System.out.println("Image loaded and scaled to: " + targetW + "x" + targetH);
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
                    // Draw the (pre-scaled) background image to fill the component
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
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
