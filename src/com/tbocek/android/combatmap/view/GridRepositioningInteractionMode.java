package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.PointF;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Defines an interaction mode that lets the user scroll and pinch-zoom to move
 * and resize the grid without affecting anything that has been drawn.
 * @author Tim Bocek
 *
 */
public final class GridRepositioningInteractionMode
		extends CombatViewInteractionMode {

	/**
	 * Constructor.
	 * @param view The CombatView to interact with.
	 */
    public GridRepositioningInteractionMode(final CombatView view) {
        super(view);
    }

    @Override
    public boolean onScroll(
    		final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
        view.getData().getGrid().gridSpaceToWorldSpaceTransformer().moveOrigin(
                -view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		distanceX),
                -view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		distanceY));
        view.refreshMap();
        return true;
    }

    @Override
    public boolean onScale(final ScaleGestureDetector detector) {
        PointF invariantPointWorldSpace =
        	view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
        			detector.getFocusX(), detector.getFocusY());
        view.getData().getGrid().gridSpaceToWorldSpaceTransformer().zoom(
        		detector.getScaleFactor(), invariantPointWorldSpace);
        view.refreshMap();
        return true;
    }

}
