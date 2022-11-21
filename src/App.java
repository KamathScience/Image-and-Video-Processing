
/**
 * The App class contains the main method. 
 * Main method is responsible for instantiating the ReadFrame, ReadShots and MainFrame object.
 * 
 * @author Divya Kamath
 */

import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) throws Exception {
        int startFrame = 1000;
        int endFrame = 5000;
        int gtTor = 2;
        String videoPath = "src/video.mpeg";

        // Displays progress bar
        Thread progressBarThread = new Thread(new ProgressBar());
        progressBarThread.start();

        // Reads the frames of the video in the video path starting
        // from startFrame to endFrame and calculates the frame distances
        ReadFrames readFrames = new ReadFrames(startFrame, endFrame, videoPath);
        readFrames.grabFrames();

        // ReadShots identifies cuts and gradual transitions
        ReadShots readShots = new ReadShots(startFrame, gtTor);
        readShots.setThreshold();
        readShots.detectCuts();
        readShots.detectGradualTransition();

        // Displays all the shots
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame app = new MainFrame(videoPath, startFrame, endFrame);
                app.setVisible(true);
            }
        });

    }
}
