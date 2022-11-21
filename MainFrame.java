/*
 *  
 * The MainFrame sets up the GUI of application.
 * 
 * It is responsible for the following three things 
 * (a) Set up the GUI with a Frame containing two panels embedded in it.
 * (b) Read the shots and displays the first frame on GUI.
 * (c) Use ffmpeg to cut videos based on the second frame of each shot 
 *     till the first frame of its next shot
 * 
 * @author Divya Kamath
 */

import java.awt.image.BufferedImage;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.HashMap;
import java.io.IOException;
import java.lang.Thread.State;

public class MainFrame extends JFrame implements ActionListener {

    private Toolkit tk;
    // The main frame contains two panels, rightPanel to display the first frame of
    // selected shot and leftPanel to display the shots in order

    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel selectedImage;
    private JPanel imageOrder;
    private JButton reset;
    private JButton play;
    private ImageIcon defaultImage;

    private int selectedOrderID;
    private int frameRate;
    private final int startIndex;
    private final int endIndex;
    private final String videoFormat;
    private final String imageFormat;
    private final String videoClipStoragePath;
    private final String videoFile;
    private final String defaultImageName;

    private final PriorityQueue<int[]> shots;
    private Map<Integer, JButton> buttonMap;

    /**
     * constructor
     * 
     * pre: videoFile, startIndex and endIndex should be passed as the parameters
     * 
     * post: Sets up the default GUI and display the first frame of shots
     * 
     */

    MainFrame(String videoFile, int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.videoFile = videoFile;
        this.shots = ReadShots.shots;
        tk = this.getToolkit();
        videoFormat = ".avi";
        imageFormat = ".jpg";
        defaultImageName = "videoCut.png";
        videoClipStoragePath = "src/videos/";

        // HEADING START
        JLabel heading = new JLabel();
        heading.setText("VIDEO SHOT DETECTION");
        heading.setBounds(350, 0, 575, 100);
        heading.setForeground(new Color(0xe1f5fe));
        heading.setFont(new Font("Lora", Font.PLAIN, 25));
        // HEADING END

        // LEFT PANEL START
        leftPanel = new JPanel();
        leftPanel.setBounds(35, 90, 450, 500);
        leftPanel.setBackground(new Color(0x39796b));
        buttonMap = new HashMap<Integer, JButton>();
        getShots();
        imageOder();
        // LEFT PANEL ENDS

        // RIGHT PANEL STARTS
        rightPanel = new JPanel();
        rightPanel.setBackground(new Color(0x39796b));
        rightPanel.setBounds(530, 90, 450, 500);
        rightPanel.setLayout(null);
        // Attribution for the image videoCut.png
        // videoCut.png icon is made by Freepik from www.flaticon.com

        java.net.URL defaultImageUrl = this.getClass().getResource(defaultImageName);
        defaultImage = new ImageIcon(tk.getImage(defaultImageUrl));
        Image image = defaultImage.getImage();
        Image newing = image.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
        defaultImage = new ImageIcon(newing);

        selectedImage = new JLabel();
        selectedImage.setBounds(50, 20, 350, 350);

        play = new JButton();
        play.setText("PLAY ON NEW WINDOW");
        play.setFocusable(false);
        play.setFont(new Font("Comic Sans", Font.BOLD, 13));
        play.setBounds(15, 425, 200, 40);

        reset = new JButton();
        reset.setText("RESET-CLOSE POP-UPS");
        reset.setFocusable(false);
        reset.setFont(new Font("Comic Sans", Font.BOLD, 13));
        reset.setBounds(235, 425, 200, 40);

        play.addActionListener(new PlayButtonHandler());
        reset.addActionListener(new ResetButtonHandler());

        rightPanel.add(selectedImage);
        rightPanel.add(play);
        rightPanel.add(reset);
        // RIGHT PANEL ENDS

        // MAIN FRAME START
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1024, 1024);
        this.setResizable(false);
        this.getContentPane().setBackground(new Color(0x263238));
        this.setLocationRelativeTo(null);
        this.setTitle("Video Shot Boundary Detection System");

        this.add(heading);
        this.add(leftPanel);
        this.add(rightPanel);

        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        // MAIN FRAME END

        defaultSetting();
    }

    /**
     * getVideos method is responsible to cut videos according to the start and end
     * Frame number.
     * getVideos uses ffmpeg command to cut the videos.
     * 
     * pre: start and end frame are passed along with count which is used to save
     * the videoName.
     * 
     * post: multiple shot videos are stored in the decided videoClipStoragePath.
     */

    private void getVideos(int startFrame, int endFrame, int count) {
        String input = videoFile;
        // gives the time at which the frame is played in the original video
        double start = (double) startFrame / frameRate;
        String startTime = Double.toString(start);

        double end = (double) endFrame / frameRate;
        String endTime = Double.toString(end);

        String output = videoClipStoragePath + count + videoFormat;
        String[] cmd = { "ffmpeg", "-y", "-ss", startTime, "-to", endTime, "-i", input, output };

        try {
            new ProcessBuilder(Arrays.asList(cmd)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * getShots method is responsible to capture the first frame and the respective
     * shot of each cut and gradual transition.
     * 
     * pre: shot priority queue is initialised and is loaded with Cs Ce Fs and Fe
     * values
     * 
     * post: capture the first frame and the respective shot video of each cut and
     * gradual transition.
     */

    private void getShots() {
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFile)) {
                frameGrabber.start();
                frameRate = (int) frameGrabber.getVideoFrameRate();

                int id = 0;
                int count = 0;
                boolean isLast = false;
                BufferedImage buffImageFrame;
                Frame tempFrame = frameGrabber.grabImage();

                int[] nextShot = shots.poll();
                int[] currentShot = new int[] { startIndex, nextShot[0] };

                while (tempFrame != null) {

                    if (count == currentShot[0]) {
                        buffImageFrame = converter.convert(tempFrame);
                        captureImage(buffImageFrame, currentShot, id);
                        getVideos(currentShot[0], currentShot[1], id);
                        id++;
                        if (isLast) {
                            break;
                        }
                        currentShot[0] = nextShot[0] + 1;
                        nextShot = shots.poll();
                        if (nextShot == null) {
                            currentShot[1] = endIndex;
                            isLast = true;
                        } else {
                            currentShot[1] = nextShot[0];
                        }
                    }
                    tempFrame = frameGrabber.grabImage();
                    count++;
                }
            } catch (org.bytedeco.javacv.FFmpegFrameGrabber.Exception e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (org.bytedeco.javacv.FFmpegFrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * captureImage method is responsible to capture the first frame of the
     * respective
     * cut or gradual transition.
     * 
     * pre: buttonMap should be initialised
     * 
     * post: capture the first frame of respective cut or gradual transition.
     * 
     */

    private void captureImage(BufferedImage image, int[] currentShot, int id) {
        ImageIcon icon = new ImageIcon(image);
        if (icon != null) {

            Image imageIcon = icon.getImage();
            Image newingIcon = imageIcon.getScaledInstance(150, 150,
                    java.awt.Image.SCALE_SMOOTH);
            icon = new ImageIcon(newingIcon);

            JButton imgButton = new JButton(icon);
            imgButton.setToolTipText((currentShot[0]) + imageFormat);
            imgButton.setPreferredSize(new Dimension(75, 100));

            imgButton.addActionListener(new IconButtonHandler(icon, id));
            imgButton.setDisabledIcon(icon);
            buttonMap.put(id, imgButton);
        }
    }

    /**
     * ImageOrder method is responsible to diplay the images in the leftPanel.
     * It displays the images as per the key in buttonMap
     * 
     * pre: leftPanel, buttonHashMap and buttonMap are instantiated
     * 
     * post: Adds scroll to leftPanel. Scroll is populated with imageOrder that
     * holds all the image in desired order.
     */

    private void imageOder() {
        imageOrder = new JPanel(new GridLayout(10, 4, 5, 5));
        imageOrder.setBounds(35, 90, 450, 500);
        imageOrder.setBackground(new Color(0x39796b));

        for (int i = 0; i < buttonMap.size(); i++) {
            imageOrder.add(buttonMap.get(i));
        }
        JScrollPane scroll = new JScrollPane(imageOrder, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setEnabled(true);
        scroll.setPreferredSize(new Dimension(450, 500));
        scroll.getVerticalScrollBar().setUnitIncrement(500);
        leftPanel.add(scroll);
        scroll.getVerticalScrollBar().setValue(0);
    }

    /**
     * IconButtonHandler implements an ActionListener for each iconButton.
     * 
     * pre: none
     * 
     * post: When an iconButton is clicked, it makes 3 changes
     * (a) it sets the selectedImage to the image on the button
     * (b) it sets selectedOrderID to the image id selected
     * (c) enables the play and reset button
     * 
     */

    private class IconButtonHandler implements ActionListener {
        ImageIcon selectedButtonImage;
        int id;

        IconButtonHandler(ImageIcon img, int id) {
            selectedButtonImage = img;
            this.id = id;
            Image imageIcon = selectedButtonImage.getImage();
            Image newingIcon = imageIcon.getScaledInstance(350, 350,
                    java.awt.Image.SCALE_SMOOTH);
            selectedButtonImage = new ImageIcon(newingIcon);
        }

        public void actionPerformed(ActionEvent e) {
            selectedImage.setIcon(selectedButtonImage);
            selectedImage.setToolTipText("Click the Play button to play the clip");
            reset.setEnabled(true);
            play.setEnabled(true);
            selectedOrderID = id;
        }
    }

    /**
     * defaultSetting method sets the GUI to its default setting
     * 
     * pre: none
     * 
     * post: It makes the following 3 changes
     * (a) selectedImage icon is set to default image
     * (b) Closes all the open windows other than the main application
     * (c) disables the play and reset button
     * 
     */

    private void defaultSetting() {
        selectedImage.setIcon(defaultImage);
        selectedImage.setToolTipText("Select a clip from left");
        play.setEnabled(false);
        reset.setEnabled(false);
        Window[] openWindows = java.awt.Window.getWindows();
        for (int i = 0; i < openWindows.length; i++) {
            if (new String("MainFrame").equals(openWindows[i].toString().substring(0, 9))) {
                continue;
            }

            openWindows[i].dispose();
        }
    }

    /**
     * PlayButtonHandler implements an ActionListener for the play button.
     * 
     * pre: play button is enabled and selectedOrderID is set to the right oreder id
     * 
     * post: When an reset button is clicked, it creates a new thread to play the
     * respective video shot using the selectedOrderID
     * 
     */

    private class PlayButtonHandler implements ActionListener {

        PlayButtonHandler() {
        }

        public void actionPerformed(ActionEvent e) {
            Thread thread = new Thread(new PlayVideo(videoClipStoragePath + selectedOrderID + videoFormat));
            thread.start();
            while (thread.getState() == State.RUNNABLE) {
            }
            play.setEnabled(false);
        }
    }

    /**
     * ResetButtonHandler implements an ActionListener for the reset button.
     * 
     * pre: reset button is enabled
     * 
     * post: When an reset button is clicked,it calls the defaultSetting method
     * 
     */

    private class ResetButtonHandler implements ActionListener {
        ResetButtonHandler() {
        }

        public void actionPerformed(ActionEvent e) {
            defaultSetting();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

}
