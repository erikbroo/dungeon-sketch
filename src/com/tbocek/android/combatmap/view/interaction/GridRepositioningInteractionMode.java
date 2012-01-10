package com.tbocek.android.combatmap.view.interaction;

import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

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
        getView().getData().getGrid().gridSpaceToWorldSpaceTransformer().moveOrigin(
                -getView().getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		distanceX),
                -getView().getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		distanceY));
        getView().refreshMap();
        return true;
    }

    @Override
    public boolean onScale(final ScaleGestureDetector detector) {
        PointF invariantPointWorldSpace =
        	getView().getWorldSpaceTransformer().screenSpaceToWorldSpace(
        			detector.getFocusX(), detector.getFocusY());
        getView().getData().getGrid().gridSpaceToWorldSpaceTransformer().zoom(
        		detector.getScaleFactor(), invariantPointWorldSpace);
        getView().refreshMap();
        return true;
    }

}
