package com.tbocek.android.combatmap;

import com.tbocek.android.combatmap.model.primitives.Util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * An Android dialog that allows the user to enter text.  Provides a text entry
 * field, and a confirm button.  The system back button is used to cancel.
 * Allows customization of the dialog title and the text displayed on the
 * confirmation button.
 * @author Tim Bocek
 *
 */
public class FontDialog extends Dialog {


    /**
     * Listener used to specify the action to take when the user confirms text
     * entry in a TextPromptDialog.
     * @author Tim Bocek
     *
     */
    public interface OnTextConfirmedListener {
        /**
         * Called when the user confirms the text entered.
         * @param text The text entered by the user.
         * @param fontSize The font size specified in the dialog.
         */
        void onTextConfirmed(String text, float fontSize);
    }


    /**
     * Text entry field.
     */
    private TextView mNameText;
    
    /**
     * Spinner for font size selection.
     */
    private Spinner mFontSize;

    /**
     * Button that the user clicks to confirm the text entered.
     */
    private Button mConfirmButton;

    /**
     * Listener that is called when the user clicks the confirm button.
     */
    private OnTextConfirmedListener mListener;

    /**
     * Constructor.
     * @param context Context to create the dialog in.
     * @param listener Listener that specifies the action to take when the user
     * 		confirms the text entered.
     */
    public FontDialog(
            final Context context, final OnTextConfirmedListener listener) {
        super(context);
        this.setContentView(R.layout.draw_text);
        this.setTitle(context.getString(R.string.draw_text));
        this.mListener = listener;

        mConfirmButton = (Button) this.findViewById(R.id.button_save);
        mNameText = (TextView) this.findViewById(R.id.entered_text);
        mNameText.requestFocus();
        mNameText.setText("");
        
        mFontSize = (Spinner) this.findViewById(R.id.spinner_font_size);

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String name = (String) mNameText.getText().toString();
                float size = Float.parseFloat(mFontSize.getSelectedItem().toString());
                dismiss();
                FontDialog.this.mListener.onTextConfirmed(name, size);
            }
        });
    }

    /**
     * Sets the value of the text and font size fields, for when this dialog
     * is used to edit instead of create text objects.
     * @param text The current text of the object being edited.
     * @param textSize The current font size of the object being edited.
     */
	public void populateFields(String text, float textSize) {
		mNameText.setText(text);
		
		// Iterate through the font size items, select the one that best fits
		// the provided number
		for (int i = 0; i < mFontSize.getCount(); ++i) {
			float parsedItem = Float.parseFloat(mFontSize.getItemAtPosition(i).toString());
			if (Math.abs(textSize - parsedItem) < Util.FP_COMPARE_ERROR) {
				mFontSize.setSelection(i);
			}
		}
	}

	/**
	 * Removes any currently entered text from the dialog.
	 */
	public void clearText() {
		mNameText.setText("");
	}
}
