package com.tbocek.android.combatmap.view;


import android.view.MotionEvent;


/**
 * Extends the default combat view gesture behavior to allow a single finger to
 * scroll around the map area.
 *
 * @author Tim Bocek
 */
public class ZoomPanInteractionMode extends CombatViewInteractionMode {
    /**
     * Constructor.
     * @param view The CombatView that this interaction mode interacts with.
     */
    public ZoomPanInteractionMode(final CombatView view) {
        super(view);
    }

    @Override
    public boolean onScroll(
            final MotionEvent e1, final MotionEvent e2,
            final float distanceX, final float distanceY) {
        view.getTransformer().moveOrigin(-distanceX, -distanceY);
        view.refreshMap();
        return true;
    }
}
