package com.tbocek.android.combatmap.view.interaction;


import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

/**
 * This interaction mode allows the user to move their finger to erase points
 * that have been drawn on the canvas.  Erases only the active set of lines.
 * @author Tim Bocek
 *
 */
public final class EraserInteractionMode extends BaseDrawInteractionMode {
	/**
	 * Radius in screen space to erase out from the center of a touch event.
	 */
    private static final float ERASER_RADIUS = 30;

    /**
     * The color of the eraser to draw on the screen.
     */
    private static final int ERASER_COLOR = Color.rgb(180, 180, 180);

    /**
     * True if currently actively erasing.
     */
    private boolean erasing;

    /**
     * The last point that was erased.  Used so that we can draw a circle there
     * in the draw pass.
     */
    private PointF lastErasedPoint;

    /**
     * Constructor.
     * @param view The combat view to interact with.
     */
    public EraserInteractionMode(final CombatView view) {
        super(view);
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
        // Set up to draw erase indicator
        this.erasing = true;
        this.lastErasedPoint = new PointF(e2.getX(), e2.getY());

        // Erase
        view.getActiveLines().erase(
        		view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
        				this.lastErasedPoint),
                view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
                		ERASER_RADIUS));

        view.refreshMap();
        return true;
    }

    @Override
    public void onUp(final MotionEvent event) {
        this.erasing = false;
        this.view.optimizeActiveLines();
        view.refreshMap();
    }

    @Override
    public void draw(final Canvas c) {
        Paint p = new Paint();
        //Draw a light grey circle showing the erase diameter.
        p.setColor(ERASER_COLOR);

        if (erasing) {
            c.drawCircle(
            		lastErasedPoint.x, lastErasedPoint.y, ERASER_RADIUS, p);
        }
    }

}
