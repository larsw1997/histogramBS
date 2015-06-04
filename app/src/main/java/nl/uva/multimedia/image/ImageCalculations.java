package nl.uva.multimedia.image;

import java.util.Arrays;

/**
 * Class containing calculation function to get the mean, median and standard deviation
 * of all green values in an array of argb values.
 */
public class ImageCalculations {
    private int[] argb, greenValues;
    private int mean, median, stdDev;

    /**
     * Constructor, calls calcGreen to calculate all stats
     * @param argb - array of argb values
     */
    public ImageCalculations(int[] argb) {
        this.argb = argb;
        this.greenValues = this.calcGreen();
    }

    /**
     * Calculate mean, median and standard deviation of all green values in argb
     */
    public int[] calcGreen() {
        int[] greenVals = new int[argb.length];

        // Reset variables
        mean = median = stdDev = 0;

        // Put all green values into array, calculate the sum of all values in mean
        for(int i = 0; i < argb.length; i++) {
            // Get the green value byte by shifting it back
            greenVals[i] = argb[i] >> 8 & 255;
            mean += greenVals[i];
        }

        // Calculate the mean
        mean = mean / greenVals.length;

        // Calculate other stats
        calcMedian(greenVals);
        calcStdDev(greenVals);

        return greenVals;
    }

    /**
     * Calculate the median of the green values
     * @param greenVals - array containing all green values
     */
    private void calcMedian(int[] greenVals) {
        // Sort the array
        Arrays.sort(greenVals);
        int medianL, medianR;
        int middle = greenVals.length / 2;

        if(greenVals.length % 2 == 0) {
            // If there is not one middle value (= the amount of green values is even),
            // take the two middle values and calculate their mean
            medianL = greenVals[middle];
            medianR = greenVals[middle - 1];
            median = (medianL + medianR) / 2;
        } else {
            // Else, there is one middle value, which is the median
            median = greenVals[middle + 1];
        }
    }

    /**
     * Calculate the standard deviation of the green values
     * @param greenVals - array containing all green values
     */
    private void calcStdDev(int[] greenVals) {
        double temp = 0;

        for(double a : greenVals)
            temp += (mean - a) * (mean - a);

        stdDev = (int)Math.sqrt(temp / greenVals.length);
    }

    public int getMean() { return mean; }
    public int getMedian() { return median; }
    public int getStdDev() { return stdDev; }
    public int[] getGreenValues() { return greenValues; }
}
