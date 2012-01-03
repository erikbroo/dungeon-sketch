package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;

public class AboutDialog extends Dialog {
    public AboutDialog(final Context context) {
        super(context);
        this.setContentView(R.layout.about_box);
        this.setTitle("About Dungeon Sketch");
    }
}
