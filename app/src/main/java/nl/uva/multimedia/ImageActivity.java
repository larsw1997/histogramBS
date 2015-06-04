/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 * Aanpassing door Lars Wenker en Bart van den Aardweg
 */

package nl.uva.multimedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

import nl.uva.multimedia.image.CameraImageSource;
import nl.uva.multimedia.image.FileImageSource;
import nl.uva.multimedia.image.ImageDisplayView;

/*
 * An activity containing the basics for an image processing application.
 */
public class ImageActivity extends Activity {

    /*** Source constants (position in list) ***/
    final int SOURCE_BACK_CAMERA = 0;
    final int SOURCE_FRONT_CAMERA = 1;
    final int SOURCE_IMAGE = 2;
    private int binCount = 0;
    private int counter = 0;
    private CameraImageSource cis;
    private FileImageSource fis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image);

        /* Create sources: */
        this.cis = new CameraImageSource(this);
        this.fis = new FileImageSource();

        Spinner sourceSpinner = (Spinner)this.findViewById(R.id.source_spinner);

        /* Switching between sources: */
        sourceSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case SOURCE_BACK_CAMERA:
                                ImageActivity.this.cis.switchTo(CameraImageSource.BACK_CAMERA);
                                ImageActivity.this.switchToCamera();
                                break;
                            case SOURCE_FRONT_CAMERA:
                                ImageActivity.this.cis.switchTo(CameraImageSource.FRONT_CAMERA);
                                ImageActivity.this.switchToCamera();
                                break;
                            case SOURCE_IMAGE:
                                ImageActivity.this.switchToImage();
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        /* Freeze switch: */
        ((CompoundButton)findViewById(R.id.freeze_toggle)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ImageActivity.this.cis.setFrozen(isChecked);
                    }
                });

        /* binCount text */
        final TextView t = (TextView)findViewById(R.id.text_view);
        t.setText(Integer.toString(binCount));

        /* Seek Bar */
        ((SeekBar)findViewById(R.id.seek_bar)).setOnSeekBarChangeListener(
                /* Listener for the seekbar */
                new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        binCount = progress + 1;
                        t.setText(Integer.toString(binCount));
                        ImageDisplayView.setbinCount(binCount);

                        /* Checks if the freeze button is checked, and manually calls the invalidate
                         * function to redraw the graph when bin count is changed during freeze. The
                         * counter ensures only every first or 10th change is redrawn to increase
                         * performance.
                         */
                        CompoundButton freeze = (CompoundButton)findViewById(R.id.freeze_toggle);
                        if(freeze.isChecked()) {
                            counter++;
                            if(counter == 1 || counter == 10) {
                                ImageDisplayView idv = (ImageDisplayView) findViewById(R.id.display_view);
                                idv.invalidate();
                                if(counter == 10) {
                                    counter = 0;
                                }
                            }
                        }
                   }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });



        /* "Load image" button: */
        findViewById(R.id.load_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Start an external Activity for choosing an image, result is returned in
                 * onActivityResult(). */
                Intent it = new Intent();
                it.setType("image/*");
                it.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(it, "Load image from..."), SOURCE_IMAGE);
            }
        });

        /* Select the back camera as default source: */
        sourceSpinner.setSelection(SOURCE_BACK_CAMERA);
    }



    private void switchToCamera() {
        /* Set camera as active source: */
        ImageDisplayView idv = (ImageDisplayView)findViewById(R.id.display_view);
        if (idv.getImageSource() != this.cis) {
            idv.setImageSource(this.cis);
        }
        ImageDisplayView.setbinCount(binCount);
        /* Switch out controls: */
        findViewById(R.id.load_image_button).setVisibility(View.GONE);
        findViewById(R.id.freeze_control).setVisibility(View.VISIBLE);
    }

    private void switchToImage() {
        /* Set image as active source: */
        ImageDisplayView idv = (ImageDisplayView)findViewById(R.id.display_view);
        if (idv.getImageSource() != this.fis) {
            idv.setImageSource(this.fis);
        }
        ImageDisplayView.setbinCount(binCount);
        /* Switch out controls: */
        findViewById(R.id.load_image_button).setVisibility(View.VISIBLE);
        findViewById(R.id.freeze_control).setVisibility(View.GONE);
    }

    /* When an image is loaded, the result gets delivered back here: */
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);

        /* If we have an image... */
        if (requestCode == SOURCE_IMAGE && resultCode == RESULT_OK && it != null) {
            try {
                /* ...open an input stream and pass it to the FileImageSource: */
                fis.loadFromInputStream(this.getContentResolver().openInputStream(it.getData()));
            } catch (FileNotFoundException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

}
