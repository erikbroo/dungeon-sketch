package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.PointF;

import android.view.MotionEvent;

/**
 * Provides and interaction mode where tapping a region (in the fog of war)
 * deletes it.
 * @author Tim
 *
 */
public final class DeleteRegionInteractionMode
		extends CombatViewInteractionMode {

	/**
	 * Constructor.
	 * @param view The view to interact with.
	 */
	public DeleteRegionInteractionMode(final CombatView view) {
		super(view);
	}

    @Override
    public boolean onSingleTapUp(final MotionEvent ev) {
    	PointF tap = view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
    			ev.getX(), ev.getY());
    	view.getActiveLines().deleteRegionsUnderPoint(tap);
    	view.refreshMap();
        return true;
    }
}
