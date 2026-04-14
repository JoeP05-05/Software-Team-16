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
        //setupCountBox();
        setupWindow();

        //Allows the box to appear on the screen
        //setupBackground();
        setupUI();


        //Where the actual timer is calculated
        count_timer();
    }


    private void setupWindow() 
    {
        setSize(250, 250);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - getWidth()) / 2;
        int y = (screen.height - getHeight()) / 2;

        setLocation(x, y);
    }


    private void setupUI() 
    {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BorderLayout());

        timerLabel = new JLabel(String.valueOf(time_left), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 120));
        timerLabel.setForeground(Color.WHITE);

        panel.add(timerLabel, BorderLayout.CENTER);
        setContentPane(panel);
        setVisible(true);
    }


    private void count_timer()
    {
        Timer countdown = new Timer(1000, new ActionListener() {
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
