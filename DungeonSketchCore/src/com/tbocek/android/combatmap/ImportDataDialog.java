package com.tbocek.android.combatmap;

import roboguice.activity.RoboActivity;
import android.os.Bundle;

/**
 * Dialog that allows importing data from other installed instances of Dungeon
 * Sketch.
 * @author Tim
 *
 */
public class ImportDataDialog extends RoboActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.import_data);
    }

}
