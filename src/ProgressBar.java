
/**
 * 
 * The ProgressBar displays progress bar when the application 
 * reads the frame and calculates the shots.
 * 
 * @author Divya Kamath
 */

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressBar implements Runnable {
    // Frame to display progress bar
    JFrame frame = new JFrame();
    JProgressBar bar = new JProgressBar(0, 100);

    /**
     * constructor
     * 
     * pre: none
     * 
     * post: sets the frame and progress bar settings.
     */

    ProgressBar() {
        bar.setValue(0);
        bar.setBounds(50, 75, 400, 50);
        bar.setStringPainted(true);

        frame.add(bar);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setSize(500, 300);
        frame.setTitle("Running Twin comparision algorithm");
        frame.setLayout(null);
        frame.setVisible(true);
    }

    /**
     * run method is responsible to run the thread.
     * 
     * pre: thread is initialised.
     * 
     * post: displays the progressbar in a new window
     */
    @Override
    public void run() {
        int count = 0;
        // counter starts at 0 and increments till 100
        while (count <= 100) {
            bar.setValue(count);
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count += 1;
        }
        bar.setString("Hang tight directing to the main application");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // close the frame
        frame.dispose();
    }
}
