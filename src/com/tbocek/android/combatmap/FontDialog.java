package com.tbocek.android.combatmap;

import com.tbocek.android.combatmap.FontDialog.OnTextConfirmedListener;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
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
         */
        void onTextConfirmed(String text, float fontSize);
    }


    /**
     * Text entry field.
     */
    private TextView nameText;
    
    private Spinner fontSize;

    /**
     * Button that the user clicks to confirm the text entered.
     */
    private Button confirmButton;

    /**
     * Listener that is called when the user clicks the confirm button.
     */
    private OnTextConfirmedListener mListener;

    /**
     * Constructor.
     * @param context Context to create the dialog in.
     * @param listener Listener that specifies the action to take when the user
     * 		confirms the text entered.
     * @param title Title to display in the dialog.
     * @param confirmText Text to display on the confirmation button.
     */
    public FontDialog(
            final Context context, final OnTextConfirmedListener listener) {
        super(context);
        this.setContentView(R.layout.draw_text);
        this.setTitle("Draw Text");
        this.mListener = listener;

        confirmButton = (Button) this.findViewById(R.id.button_save);
        nameText = (TextView) this.findViewById(R.id.entered_text);
        nameText.requestFocus();
        nameText.setText("");
        
        fontSize = (Spinner) this.findViewById(R.id.spinner_font_size);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String name = (String) nameText.getText().toString();
                float size = Float.parseFloat(fontSize.getSelectedItem().toString());
                dismiss();
                FontDialog.this.mListener.onTextConfirmed(name, size);
            }
        });
    }

	/**
     * On resuming the activity, clears the text so that nothing is left over
     * from the last time the dialog was opened.
     */
    protected final void onResume() {
        nameText.setText("");
    }

	public void populateFields(String text, float textSize) {
		nameText.setText(text);
		//TODO: Set the font size.
	}
}
