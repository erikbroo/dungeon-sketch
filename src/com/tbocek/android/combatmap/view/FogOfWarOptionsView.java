package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.tbocek.android.combatmap.R;

/**
 * Custom DrawOptionsView that is tailored to the fog of war view.
 * @author Tim
 *
 */
public class FogOfWarOptionsView extends DrawOptionsViewBase {
    /**
     * Constructs a new DrawOptionsView.
     * @param context The context to construct in.
     */
    public FogOfWarOptionsView(final Context context) {
        super(context);
        layout = new LinearLayout(context);
        addView(layout);

        createAndAddPanButton();
        createAndAddEraserButton();
        createAndAddUndoButton();
        createAndAddRedoButton();

        ImageToggleButton b = new ImageToggleButton(this.getContext());
        b.setImageResource(R.drawable.pencil);
        b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
	            onChangeDrawToolListener.onChooseStrokeWidth(0);
	            untoggleGroup(toolsGroup);
	            ((ImageToggleButton) v).setToggled(true);
			}
        });
        layout.addView(b);
        toolsGroup.add(b);
    }
}
