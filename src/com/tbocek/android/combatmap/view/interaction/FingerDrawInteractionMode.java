package com.tbocek.android.combatmap.view.interaction;

import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.view.CombatView;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Interaction mode that allows the user to draw a line on the CombatView.
 * The color and width of the line are selected and stored elsewhere in the
 * CombatView.
 * @author Tim
 *
 */
public class FingerDrawInteractionMode extends BaseDrawInteractionMode {

    /**
     * Distance between successive points on a line in screen space.
     */
    private static final float POINT_RATE_LIMIT = 3;

    /**
     * The last x coordinate at which a point was added to the current line.
     */
    private float mLastPointX;

    /**
     * The last y coordinate at which a point was added to the current line.
     */
    private float mLastPointY;

    /**
     * The line that the user is actively drawing.  New points will be added to
     * this line.
     */
    private Shape mCurrentLine;
    
    /**
     * Whether a zoom operation is in progress.
     */
    private boolean mZooming;
    
    /**
     * Whether a draw operation is in progress.
     */
    private boolean mDrawing;


	/**
     * Constructor.
     * @param view The CombatView to manipulate.
     */
    public FingerDrawInteractionMode(final CombatView view) {
        super(view);
    }
    
    @Override
    public boolean onScale(final ScaleGestureDetector detector) {
    	mZooming = true;
    	if (!mDrawing) {
    		return super.onScale(detector);
    	} else {
    		return true;
    	}
    }


    @Override
    public boolean onScroll(
            final MotionEvent e1, final MotionEvent e2,
            final float distanceX, final float distanceY) {
    	if (mZooming) {
            getView().getWorldSpaceTransformer().moveOrigin(
            		-distanceX, -distanceY);
            getView().refreshMap();
    		return true;
    	}
    	
        if (mCurrentLine == null) {
            return true;
        }

    	mDrawing = true;
    	
        if (shouldAddPoint(e2.getX(), e2.getY())) {
            addLinePoint(e2);
        }
        return true;
    }

    /**
     * Adds the location of the given motion event to the line.
     * @param e The motion event containing the point to add.
     */
    private void addLinePoint(final MotionEvent e) {
        PointF p = getScreenSpacePoint(e);
    	// Need to transform to world space.
	    mCurrentLine.addPoint(
	            getView().getWorldSpaceTransformer()
	            		.screenSpaceToWorldSpace(p));

        getView().refreshMap(); // Redraw the screen
        mLastPointX = p.x;
        mLastPointY = p.y;
    }

    /**
     * Gets the draw location in screen space.  Snaps to the grid if necessary.
     * @param e The motion event to get the point from.
     * @return The point in screen space.
     */
    private PointF getScreenSpacePoint(final MotionEvent e) {
    	PointF p = new PointF(e.getX(), e.getY());
    	if (getView().shouldSnapToGrid()) {
    		CoordinateTransformer transformer
    				= getView().getGridSpaceTransformer();
    		p = transformer.worldSpaceToScreenSpace(
    				getView().getData().getGrid().getNearestSnapPoint(
    						transformer.screenSpaceToWorldSpace(p), 0));
    	}
    	return p;
    }

    /**
     * Returns True if the proposed point is far enough away from the previously
     * drawn point to add to the line.
     * @param newPointX X coordinate of the new point.
     * @param newPointY Y coordinate of the new point.
     * @return True if the point should be added
     */
    private boolean shouldAddPoint(
            final float newPointX, final float newPointY) {
        return Util.distance(mLastPointX, mLastPointY, newPointX, newPointY)
                > POINT_RATE_LIMIT;
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        mCurrentLine = createLine();
        PointF p = getScreenSpacePoint(e);
        mLastPointX = p.x;
        mLastPointY = p.y;
        return true;
    }
    
    @Override
    public void onUp(final MotionEvent e) {
    	if (getNumberOfFingers() == 0) {
    		mZooming = false;
    		mDrawing = false;
    	}
    }
    

    /**
     * Creates a line in the data tied to the manipulated view, and returns it.
     * @return The created line.
     */
    protected Shape createLine() {
    	return getView().createLine();
    }
}
