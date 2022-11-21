/*
 * The CalculateUtil class holds all the calculation logic of the application.
 * 
 * It calculates the following four values 
 * (a) pixel RGB values
 * (b) intensity value of each image
 * (c) Manhattan distance
 * (d) average and standard deviation values.
 * 
 * @author Divya Kamath
 */

import java.awt.image.BufferedImage;

public class CalculateUtil {

    /**
     * calculatePixelValues calculates RGB value and intensity value of each pixel
     * in an image
     * 
     * pre: image, intensityMatrix and imageCount should be instantiated and passed
     * as parameters
     * 
     * post: intensityMatrix is populated with intensity values of all the selected
     * frame/images
     */

    public static void calculatePixelValues(BufferedImage image, int[][] intensityMatrix, int imageCount) {
        int intensityColumnCount = 26;
        int[] pixel;
        int[] intensityBins = new int[intensityColumnCount];
        int height = image.getHeight();
        int width = image.getWidth();
        for (int i = 0; i < intensityColumnCount; i++) {
            intensityBins[i] = 0;
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel = image.getRaster().getPixel(x, y, new int[3]);
                calculateIntensity(pixel, intensityBins);
            }
        }
        for (int i = 1; i < intensityColumnCount; i++) {
            intensityMatrix[imageCount][i] = intensityBins[i];
        }
    }

    /**
     * calculateIntensity is called by calculatePixelValues method to calculate the
     * intensity value using the formula I = 0.299R + 0.587G + 0.114B
     * 
     * pre: RGB value of a pixel.
     * 
     * post: intenisty value of the selected pixel is added to the respective
     * intensity bin.
     */
    private static void calculateIntensity(int[] pixel, int[] intensityBins) {
        double intensityDouble = (0.299 * pixel[0]) + (0.587 * pixel[1]) + (0.114 * pixel[2]);
        int intensity = (int) Math.floor(intensityDouble);
        intensity = (int) Math.floor(intensity / 10);
        if (intensity >= 24) {
            intensityBins[25] += 1;
        } else {
            intensityBins[intensity + 1] += 1;
        }
    }

    /**
     * calculateManhattanDistance method calculates the Manhattan distance between
     * two frames using its inetensity values.
     * Formaula used :SD = summation of all values from 1 to 25 | Hi(j) - Hi+1(j)|
     * 
     * pre: initialise matrix that is passed as a parameter
     * 
     * post: frame distance between two adjacent frames.
     */

    public static int[] calculateManhattanDistance(int[][] matrix) {
        int[] framDistance = new int[matrix.length - 2];
        for (int i = 1; i < matrix.length - 1; i++) {
            double distance = 0.0;
            for (int j = 1; j < matrix[i].length; j++) {
                double value = matrix[i][j] - matrix[i + 1][j];
                distance += Math.abs(value);
            }
            framDistance[i - 1] = (int) distance;
        }
        return framDistance;
    }

    /**
     * calculateAvgSD method is responsible to calculate the average and Standard
     * Deviation of frame distances
     * 
     * pre: frame distance array is populated and avgSD array is initialised before
     * passing as parameters
     * 
     * post: populates the avgSD array with average and SD value.
     */
    public static void calculateAvgSD(int[] distance, double[] avgSD) {
        // Calculate the average of each feature
        for (int i = 0; i < distance.length; i++) {
            avgSD[0] += distance[i];
        }
        avgSD[0] /= distance.length;
        // Calculate Standard Deviation
        for (int i = 0; i < distance.length; i++) {
            avgSD[1] += Math.pow(distance[i] - avgSD[0], 2);
        }
        avgSD[1] /= (double) (distance.length);// -1
        avgSD[1] = Math.sqrt(avgSD[1]);
    }
}
