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
        mView.getData().getGrid().gridSpaceToWorldSpaceTransformer().moveOrigin(
                -mView.getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		distanceX),
                -mView.getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		distanceY));
        mView.refreshMap();
        return true;
    }

    @Override
    public boolean onScale(final ScaleGestureDetector detector) {
        PointF invariantPointWorldSpace =
        	mView.getWorldSpaceTransformer().screenSpaceToWorldSpace(
        			detector.getFocusX(), detector.getFocusY());
        mView.getData().getGrid().gridSpaceToWorldSpaceTransformer().zoom(
        		detector.getScaleFactor(), invariantPointWorldSpace);
        mView.refreshMap();
        return true;
    }

}
