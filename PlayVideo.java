/*
 *  
 * The PlayVideo is responsible to play the selected video in a new window
 * It uses xuggler library to play the video and audio of a .avi file
 * 
 * @author Divya Kamath
 */

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.ToolFactory;

public class PlayVideo implements Runnable {
    // URL of the video selected
    private final String url;

    /**
     * constructor
     * 
     * pre: urlof the video is passed as the parameter
     * 
     * post: sets the url value
     */
    PlayVideo(String url) {
        this.url = url;
    }

    /**
     * run method is responsible to run the thread.
     * 
     * pre: thread is initialised.
     * 
     * post: displays the selected video in a new window
     */
    @Override
    public void run() {
        IMediaReader mediaReader = ToolFactory.makeReader(url);
        IMediaViewer mediaViewer = ToolFactory.makeViewer();
        mediaReader.addListener(mediaViewer);
        while (mediaReader.readPacket() == null)
            do {
            } while (false);
    }
}