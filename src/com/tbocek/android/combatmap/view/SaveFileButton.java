package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tbocek.android.combatmap.R;

/**
 * A button that displays the name and preview of a save file.
 * @author Tim Bocek
 *
 */
public final class SaveFileButton extends LinearLayout {

	/**
	 * The displayed preview.
	 */
    private ImageView preview;

    /**
     * The displayed file name.
     */
    private TextView text;

    /**
     * Constructor.
     * @param context The context to create this view in.
     */
    public SaveFileButton(final Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.saved_map_file, this);
        preview = (ImageView) findViewById(R.id.saved_map_preview);
        text = (TextView) findViewById(R.id.saved_map_file_name);

        // Clicking on the preview should count as clicking on the button its
        // self.
        preview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                performClick();
            }
        });
    }

    /**
     * Sets the name of the save file.
     * @param name The name to display.
     */
    public void setFileName(final String name) {
        text.setText(name);
    }

    /**
     * Sets the image to display as the preview.
     * @param image The image to display.
     */
    public void setPreviewImage(final Bitmap image) {
        preview.setImageBitmap(image);
    }

    /**
     * Returns the filename associated with this button.
     * @return The filename.
     */
    public String getFileName() {
        return text.getText().toString();
    }

    @Override
    public void setOnCreateContextMenuListener(
    		final View.OnCreateContextMenuListener l) {
        super.setOnCreateContextMenuListener(l);
        preview.setOnCreateContextMenuListener(l);
        text.setOnCreateContextMenuListener(l);
    }
}
