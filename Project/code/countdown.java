//Names: Joss Jongewaard, Kaija Frierson, Taija Frierson, Joseph Peraza 
//Team: Team 16 
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class countdown extends JWindow {

    
    private int time_left = 30;
    private JLabel timerLabel;

    public countdown()
    {
        //Sets up the window for the countdown
        setupCountBox();

        //Allows the box to appear on the screen
        setupBackground();

        //Where the actual timer is calculated
        count_timer();
    }

    private void setupCountBox()
    {
        setSize(200,200);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;

        setLocation(x, y);
    }

    private void setupBackground() {
            JPanel MainPanel = new JPanel() 
            {
                @Override
                protected void paintComponent(Graphics g) 
                {
                    super.paintComponent(g);
                    g.setColor(Color.BLACK);
                    g.drawRect(100, 100, 100, 100);
                }
            };
            MainPanel.setLayout(new BorderLayout());

        timerLabel = new JLabel("5", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 80));
        timerLabel.setForeground(Color.WHITE);
        MainPanel.add(timerLabel, BorderLayout.CENTER);
        
        setContentPane(MainPanel);
        setVisible(true);

    }

    private void count_timer()
    {
        Timer countdown = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                time_left--;
                timerLabel.setText(String.valueOf(time_left));

                if (time_left <= 0) {
                    ((Timer)e.getSource()).stop();
                    dispose();
                }
            }
        });
        countdown.start();
    }



}
