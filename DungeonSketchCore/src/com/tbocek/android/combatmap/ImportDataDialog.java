package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;

/**
 * Dialog that allows importing data from other installed instances of Dungeon
 * Sketch.
 * @author Tim
 *
 */
public class ImportDataDialog extends Dialog {

    public ImportDataDialog(Context context) {
        super(context);
        this.setContentView(R.layout.draw_text);
        this.setTitle(context.getString(R.string.import_data));
    }

}
