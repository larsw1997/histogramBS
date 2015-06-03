/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
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
    private static int binSize;
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
    private int imageWidth, imageHeight;

    /*** Green value stats ***/

    private int[] greenArray = null;
    private int mean, median, stdDev;

    @Override
    public void onImage(int[] argb, int width, int height) {
        /* When we recieve an image, simply store it and invalidate the View so it will be
         * redrawn. */
        this.currentImage = argb;
        calcGreen(argb);
        this.imageWidth = width;
        this.imageHeight = height;
        this.invalidate();
    }

    public void calcGreen(int[] argb) {
        int[] greenVals = new int[argb.length];

        mean = median = stdDev = 0;

        // Put all green values into array
        for(int i = 0; i < argb.length; i++) {
            greenVals[i] = argb[i] >> 8 & 255;
            mean += greenVals[i];
        }

        mean = mean / greenVals.length;
        calcMedian(greenVals);
        calcStdDev(greenVals);

        greenArray = greenVals;
    }

    public static void setBinSize(int newBinSize) {
        binSize = newBinSize;
    }
    
    public void calcMedian(int[] greenVals) {
        Arrays.sort(greenVals);
        int medianL, medianR;
        int middle = greenVals.length / 2;

        if(greenVals.length % 2 == 0){
            medianL = greenVals[middle];
            medianR = greenVals[middle - 1];
            median = (medianL + medianR) / 2;
        } else{
            median = greenVals[middle + 1];
        }
    }

    public void calcStdDev(int[] greenVals) {
        double temp = 0;

        for(double a : greenVals)
            temp += (mean - a) * (mean - a);
        stdDev = (int)Math.sqrt(temp / greenVals.length);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: Hier wordt een afbeelding op het scherm laten zien!
        // Je zou hier dus code kunnen plaatsen om iets anders weer te geven.

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {
            /* Center the image...
            int left = (this.getWidth() - this.imageWidth) / 2;
            int top = (this.getHeight() - this.imageHeight) / 2;

            // ...and draw it.
            canvas.drawBitmap(this.currentImage, 0, this.imageWidth, left, top, this.imageWidth,
                    this.imageHeight, true, null); */

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAlpha(255);
            canvas.drawPaint(paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(60);
            canvas.drawText("Mean: " + mean, 10, 50, paint);
            canvas.drawText("Median: " + median, 10, 100, paint);
            canvas.drawText("Std-Dev: " + stdDev, 10, 150, paint);

            if(binSize > 0) {
                int curBinSize = binSize;
                int curGreenArray[] = greenArray.clone();
                float maxHeight = (float)(this.getHeight() / 1.5);
                float maxWidth = this.getWidth();
                float binWidth = (float)((maxWidth - 40) / (double)curBinSize);
                int binHeight[] = new int[curBinSize];
                int tempHeights[] = null;
                int curBin = 0;

                for (int i = 0; i < curGreenArray.length; i++) {
                    curBin = (int)Math.floor(curGreenArray[i] / (256 / (double)curBinSize));
                    binHeight[curBin]++;
                }

                tempHeights = binHeight.clone();
                Arrays.sort(tempHeights);
                double ratio = (this.getHeight() / 2) / (double)tempHeights[tempHeights.length - 1];
                paint.setColor(Color.GREEN);

                for (int i = 0; i < curBinSize; i++) {
                    canvas.drawRect(20 + (i * binWidth), maxHeight - (float)(ratio * binHeight[i]), 20 + (i * binWidth) + binWidth, maxHeight, paint);
                }

                paint.setColor(Color.BLACK);
                canvas.drawLine(20, maxHeight + 1, maxWidth - 20, maxHeight + 1, paint);
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
