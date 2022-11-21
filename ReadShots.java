/*
 * The ReadShots calculates the cuts and gradual transitions using 
 * the frame distance values
 * 
 * It is responsible for seting the thresholds, detecting the cut and
 * detecting the gradual transitions
 * 
 * @author Divya Kamath
 */

import java.util.PriorityQueue;

public class ReadShots {
    private final int[] frameDistance;
    private final double[] avgSD;
    private double cutThreshold;
    private double gtThreshold;
    private final int gtTor;
    private final int startFrame;
    protected static PriorityQueue<int[]> shots;

    /**
     * constructor
     * 
     * pre: startFrame and gtTor values are declared.
     * 
     * post: Sets up the frameDistance, strtFrame and gtTor value. It also
     * initialises shots and avgSD arrays.
     */

    ReadShots(int startFrame, int gtTor) {
        frameDistance = ReadFrames.frameDistance;
        avgSD = new double[2];
        shots = new PriorityQueue<>((a, b) -> ((int[]) a)[0] - ((int[]) b)[0]);
        this.startFrame = startFrame;
        this.gtTor = gtTor;
    }

    /**
     * setThreshold method is responsible to set the cut, gradual transition
     * thresholds
     * Formula : For Cut : Tb = mean(SD) + std(SD) * 11;
     * For Gradual Transition Ts = mean(SD) * 2;
     * 
     * pre: initialise avgSD array and populate frame distance array.
     * 
     * post: cutThreshold and gtThreshold values are declared.
     */
    public void setThreshold() {
        CalculateUtil.calculateAvgSD(frameDistance, avgSD);
        cutThreshold = avgSD[0] + (avgSD[1] * 11);
        gtThreshold = avgSD[0] * 2;
    }

    /**
     * detectCuts method is responsible to capture the Cs frames that cross the
     * cutThreshold
     * 
     * pre: cutThreshold and framedistance values are calculated.
     * 
     * post:Adds Cs and Ce values into shots queue.
     * 
     */
    public void detectCuts() {
        System.out.println("CUTS:");
        for (int i = 0; i < frameDistance.length; i++) {
            if (frameDistance[i] >= cutThreshold) {
                shots.add(new int[] { i + startFrame, i + startFrame + 1 });
                System.out.println("Cs : " + (i + startFrame) + "     Ce : " + (i + startFrame + 1));
            }
        }
    }

    /**
     * detectGradualTransition method is responsible to capture the Fs and Fe frames
     * according to Twin comparision algorithm
     * 
     * pre: gtThreshold and framedistance values are calculated. Also, the Tor value
     * is set.
     * 
     * post:Adds Fs and Fe values into shots queue.
     * 
     */
    public void detectGradualTransition() {
        int potentialStart = Integer.MAX_VALUE;
        System.out.println("Gradual Transition");
        for (int i = 0; i < frameDistance.length; i++) {
            if (frameDistance[i] >= gtThreshold && frameDistance[i] < cutThreshold) {
                potentialStart = Math.min(potentialStart, i);
                for (int j = potentialStart + 1; j < frameDistance.length; j++) {
                    if (frameDistance[j] < gtThreshold) {
                        int torCount = 1;
                        while (torCount <= gtTor) {
                            if (frameDistance[j + torCount] < gtThreshold) {
                                torCount++;
                            } else {
                                break;
                            }
                        }
                        if (torCount >= gtTor) {
                            if (isRealTransition(potentialStart, j - 1)) {
                                shots.add(new int[] { potentialStart + startFrame, j - 1 + startFrame });
                                System.out.println(
                                        "Fs : " + (potentialStart + startFrame) + " Fe : " + (j - 1 + startFrame));
                            }
                            j += torCount;
                            i = j;
                            potentialStart = Integer.MAX_VALUE;
                            break;
                        }

                    } else if (frameDistance[j] >= cutThreshold) {
                        if (isRealTransition(potentialStart, j - 1)) {
                            shots.add(new int[] { potentialStart + startFrame, j - 1 + startFrame });
                            System.out.println(
                                    "Fs : " + (potentialStart + startFrame) + " Fe : " + (j - 1 + startFrame));
                        }
                        i = j;
                        potentialStart = Integer.MAX_VALUE;
                        break;
                    }

                }
            }
            if (potentialStart != Integer.MAX_VALUE) {
                if (isRealTransition(potentialStart, frameDistance.length - 1)) {
                    shots.add(new int[] { potentialStart + startFrame + 1, frameDistance.length - 2 + startFrame });
                    System.out.println(
                            "Fs : " + (potentialStart + startFrame) + "     Fe : "
                                    + (frameDistance.length - 1 + startFrame));
                }
                break;
            }
        }
    }

    /**
     * isRealTransition method is responsible to check if all the sum of the
     * selected SD values is greater than Cut Threshold or not.
     * 
     * pre: none
     * 
     * post:returns true if summation of values is grater than cut threshold else
     * false.
     */
    private boolean isRealTransition(int start, int end) {
        int sum = 0;
        for (int i = start; i <= end; i++) {
            sum += frameDistance[i];
        }
        return sum >= cutThreshold;
    }
}
