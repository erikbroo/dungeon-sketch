package com.tbocek.android.combatmap.view;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.Shape;

/**
 * Defines an interaction mode where the user draws on the mask layer.
 * @author Tim
 *
 */
public final class MaskDrawInteractionMode extends FingerDrawInteractionMode {


	public MaskDrawInteractionMode(CombatView view) {
		super(view);
	}

	@Override
    protected Shape createLine() {
    	return view.createFogOfWarRegion();
    }
}
