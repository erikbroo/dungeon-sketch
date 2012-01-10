package com.tbocek.android.combatmap.view.interaction;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.view.CombatView;

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
    	return mView.createFogOfWarRegion();
    }
}
