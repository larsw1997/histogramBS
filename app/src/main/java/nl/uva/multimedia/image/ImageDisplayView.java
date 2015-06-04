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
        this.invalidate();
    }

    public static void setbinCount(int newbinCount) {
        binCount = newbinCount;
    }

    private int[] curGreenArray, binHeight, tempHeights;
    private int curBin, curBinCount, graphTop, graphSize;
    private float maxHeight, maxWidth, binWidth;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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
            paint.setStrokeWidth(6);

            Paint graph = new Paint();
            graph.setColor(Color.BLACK);
            graph.setAlpha(255);
            graph.setStrokeWidth(6);
            graph.setTextSize(30);

            paint.setColor(Color.BLACK);
            paint.setTextSize(60);
            canvas.drawText("Mean: " + greenStats.getMean(), 10, 50, paint);
            canvas.drawText("Median: " + greenStats.getMedian(), 10, 100, paint);
            canvas.drawText("Std-Dev: " + greenStats.getStdDev(), 10, 150, paint);

            if(binCount > 0) {
                curBinCount = binCount;
                curGreenArray = greenArray.clone();
                maxHeight = (float)(this.getHeight() / 1.5);
                maxWidth = this.getWidth();
                binWidth = (float)((maxWidth - 200) / (double)curBinCount);
                binHeight = new int[curBinCount];
                tempHeights = null;
                curBin = 0;
                graphTop = (int)(maxHeight - this.getHeight() / 2);
                graphSize = (int)(maxHeight - graphTop);

                for (int curGreenValue : curGreenArray) {
                    curBin = (int)Math.floor(curGreenValue / (256 / (double)curBinCount));
                    binHeight[curBin]++;
                }

                tempHeights = binHeight.clone();
                Arrays.sort(tempHeights);
                double ratio = (this.getHeight() / 2) / (double)tempHeights[tempHeights.length - 1];
                paint.setColor(Color.GREEN);
                graph.setStrokeWidth(2);
                for (int i = 0; i < curBinCount; i++) {
                    canvas.drawRect(180 + (i * binWidth), maxHeight - (float)(ratio * binHeight[i]),
                            180 + (i * binWidth) + binWidth, maxHeight, paint);
                    canvas.drawLine(180 + (i * binWidth), maxHeight + 3, 180 + (i * binWidth),
                            maxHeight + 33, graph);
                }
                paint.setColor(Color.BLACK);
                /* Draws the graph lines and numbers */
                canvas.drawLine(177, maxHeight + 1, maxWidth - 20, maxHeight + 1, paint);
                canvas.drawLine(180, maxHeight + 1, 180, graphTop, paint);

                for(int i = 0; i < 10; i++) {
                    canvas.drawLine(150, graphTop + 3 + (i * graphSize) / 10, 180,
                            graphTop + 3 + (i * graphSize) / 10, graph);
                    canvas.drawText(Integer.toString((int) ((1 - (i * 0.1)) * tempHeights[tempHeights.length - 1])),
                            20, graphTop + 15 + (i * graphSize) / 10, graph);
                }
                canvas.drawLine(maxWidth - 23, maxHeight + 3, maxWidth - 23, maxHeight + 33, graph);
                canvas.drawLine(150, maxHeight + 1, 180, maxHeight + 1, graph);

                canvas.drawText("0", 20, maxHeight + 8, graph);
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
