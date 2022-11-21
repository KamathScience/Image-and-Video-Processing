/*
 *  
 * The ReadFrames reads the frames from the given video file starting 
 * from the startIndex to endIndex.
 * 
 * It is responsible for the grabing each frame to calculate its intensity
 * value and then the frame distances using Manhattan distance.
 * 
 * @author Divya Kamath
 */

import java.awt.image.BufferedImage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class ReadFrames {
    private final int startIndex;
    private final int endIndex;
    private final String videoFile;
    private final int intensityColumns;
    private int[][] intensityMatrix;
    protected static int[] frameDistance;
    private int imageCount;

    /**
     * constructor
     * 
     * pre: startIndex, endIndex and videoFile should be passed as parameters
     * 
     * post: intitalises the intensityMatrix and sets all the required values
     */

    ReadFrames(int startIndex, int endIndex, String videoFile) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.videoFile = videoFile;
        intensityColumns = 26;
        intensityMatrix = new int[endIndex - startIndex + 1][intensityColumns];
        imageCount = 1;
    }

    /**
     * grabFrames method uses FFmpegFrameGrabber to grab each frame and calculate
     * its pixel value
     * 
     * pre: videoFile value is declared
     * 
     * post: it calculates the frame distance between the adjacent frames.
     */
    public void grabFrames() {
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFile)) {
                frameGrabber.start();
                BufferedImage bffImageFrame;
                int count = 0;
                Frame tmp = frameGrabber.grabImage();
                while (tmp != null) {
                    if (count >= startIndex && count < endIndex) {
                        bffImageFrame = converter.convert(tmp);
                        CalculateUtil.calculatePixelValues(bffImageFrame, intensityMatrix, imageCount);
                        imageCount++;
                    } else if (count >= endIndex) {
                        break;
                    }
                    tmp = frameGrabber.grabImage();
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
        frameDistance = CalculateUtil.calculateManhattanDistance(intensityMatrix);
    }
}
