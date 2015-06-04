/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 * Af gemaakt door Bart van den Aardweg en Lars Wenker
 */

package nl.uva.multimedia.image;

import android.content.Context;
import android.graphics.Canvas;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.*;
import java.util.Arrays;

/*
 * This is a View that displays incoming images.
 */
public class ImageDisplayView extends View implements ImageListener {
    private static int binCount;
    /*** Constructors ***/

    public ImageDisplayView(Context context) {
        super(context);
    }

    public ImageDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*** Image drawing ***/

    private int[] currentImage = null;
    private Paint graphPaint, axePaint;

    /*** Green value stats ***/

    private int[] greenArray = null;
    private ImageCalculations greenStats = null;

    @Override
    public void onImage(int[] argb, int width, int height) {
        /* When we recieve an image, simply store it and invalidate the View so it will be
         * redrawn. */
        this.currentImage = argb;
        this.greenStats = new ImageCalculations(argb);
        this.greenArray = greenStats.getGreenValues();
        this.graphPaint = new Paint();
        this.axePaint = new Paint();
        this.invalidate();
    }

    public static void setbinCount(int newbinCount) {
        binCount = newbinCount;
    }

    /**
     * Set the graphPaint style and draw the stats mean, median and standard deviation.
     * @param canvas - to draw on
     */
    private void drawStats(Canvas canvas) {
        graphPaint.setStrokeWidth(6);
        graphPaint.setColor(Color.BLACK);
        graphPaint.setTextSize(60);

        canvas.drawText("Mean: " + greenStats.getMean(), 10, 50, graphPaint);
        canvas.drawText("Median: " + greenStats.getMedian(), 10, 100, graphPaint);
        canvas.drawText("Std-Dev: " + greenStats.getStdDev(), 10, 150, graphPaint);
    }

    /**
     * Put the greenvalue frequencies grouped per bin in an array.
     * @param curGreenArray - array containing all greenvalues
     * @param curBinCount - amount of bins
     * @return - the array containing all bin's heights
     */
    private int[] getBinHeights(int[] curGreenArray, int curBinCount) {
        int curBin;
        int[] binHeight = new int[curBinCount];

        // Loop through all greenvalues
        for (int curGreenValue : curGreenArray) {
            // Calculate the bin this value belongs to
            curBin = (int) Math.floor(curGreenValue / (256 / (double) curBinCount));
            // Increase the bin's height
            binHeight[curBin]++;
        }

        return binHeight;
    }

    /**
     * Draw the bins to the canvas.
     * @param canvas - to draw on
     * @param maxBinHeight - height of the highest bin
     * @param curBinCount - amount of bins
     * @param binHeight - array containing all bin heights
     * @param maxHeight - maximum draw height
     * @param maxWidth - maximum draw width
     * @param binWidth - width of one bin
     * @param graphTop - top of the graph
     */
    private void drawGraph(Canvas canvas, int maxBinHeight, int curBinCount, int[] binHeight,
                           float maxHeight, float maxWidth, float binWidth, int graphTop) {
        double ratio = (this.getHeight() / 2) / (double)maxBinHeight;
        graphPaint.setColor(Color.GREEN);

        // Loop through all bins
        for (int i = 0; i < curBinCount; i++) {
            // Draw the current bin
            canvas.drawRect(180 + (i * binWidth), maxHeight - (float) (ratio * binHeight[i]),
                    180 + (i * binWidth) + binWidth, maxHeight, graphPaint);
            if(i % ((curBinCount / 12) + 1) == 0) {
                canvas.drawLine(180 + (i * binWidth), maxHeight + 3, 180 + (i * binWidth),
                        maxHeight + 33, axePaint);
                canvas.drawText(Integer.toString(i), 160 + (i * binWidth), maxHeight + 63, axePaint);
            }
            else {
                canvas.drawLine(180 + (i * binWidth), maxHeight + 3, 180 + (i * binWidth),
                        maxHeight + 13, axePaint);
            }
        }
    }

    /**
     * Draws the axes for the graph
     * @param canvas - to draw on
     * @param maxBinHeight - height of the highest bin
     * @param graphTop - top of the graph
     * @param graphSize - size of the graph
     * @param maxHeight - maximum draw height
     * @param maxWidth - maximum draw width
     */
    private void drawAxes(Canvas canvas, int maxBinHeight, int graphTop, int graphSize,
                          float maxHeight, float maxWidth) {
        graphPaint.setColor(Color.BLACK);

        /* X axis */
        canvas.drawLine(177, maxHeight + 1, maxWidth - 20, maxHeight + 1, graphPaint);

        /* Y axis */
        canvas.drawLine(180, maxHeight + 1, 180, graphTop, graphPaint);

        /* Divides the y axis in 10 equal parts, and labels these gradation s */
        for(int i = 0; i < 10; i++) {
            canvas.drawLine(150, graphTop + 3 + (i * graphSize) / 10, 180,
                    graphTop + 3 + (i * graphSize) / 10, axePaint);
            canvas.drawText(Integer.toString((int) ((1 - (i * 0.1)) * maxBinHeight)),
                    20, graphTop + 15 + (i * graphSize) / 10, axePaint);
        }

        /* Marks the 0 point on the Y axis */
        canvas.drawLine(150, maxHeight + 1, 180, maxHeight + 1, axePaint);
        canvas.drawText("0", 20, maxHeight + 8, axePaint);

        // Draw axe labels
        axePaint.setTextSize(40);
        canvas.drawText("Green value bin", 450, maxHeight + 120, axePaint);
        canvas.drawText("Amount of pixels", 20, 200, axePaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {
            graphPaint.setColor(Color.WHITE);
            canvas.drawPaint(graphPaint);

            drawStats(canvas);

            axePaint.setColor(Color.BLACK);
            axePaint.setStrokeWidth(2);
            axePaint.setTextSize(30);

            if(binCount > 0) {
                int curBinCount = binCount;
                int[] curGreenArray = greenArray.clone();
                float maxHeight = (float)(this.getHeight() / 1.5);
                float maxWidth = this.getWidth();
                float binWidth = (float)((maxWidth - 200) / (double)curBinCount);
                int graphTop = (int)(maxHeight - this.getHeight() / 2);
                int graphSize = (int)(maxHeight - graphTop);

                int[] binHeight = getBinHeights(curGreenArray, curBinCount);

                // Sort a copy of the binHeights array to get the highest height (last element)
                int[] tempHeights = binHeight.clone();
                Arrays.sort(tempHeights);
                int maxBinHeight = tempHeights[tempHeights.length - 1];

                // Draw everything
                drawGraph(canvas, maxBinHeight, curBinCount, binHeight, maxHeight,
                        maxWidth, binWidth, graphTop);
                drawAxes(canvas, maxBinHeight, graphTop, graphSize, maxHeight, maxWidth);
            }

        }
    }

    /*** Source selection ***/

    private ImageSource source = null;

    public void setImageSource(ImageSource source) {
        if (this.source != null) {
            this.source.setOnImageListener(null);
        }
        source.setOnImageListener(this);
        this.source = source;
    }

    public ImageSource getImageSource() {
        return this.source;
    }
}
