package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;

/**
 * Provides a dialog for the user to export an image.
 * @author Tim
 *
 */
public class ExportImageDialog extends Dialog {

	/**
	 * Constructor.
	 * @param context Application context to use.
	 */
	public ExportImageDialog(Context context) {
		super(context);
		this.setContentView(R.layout.export_dialog);
	}

}
