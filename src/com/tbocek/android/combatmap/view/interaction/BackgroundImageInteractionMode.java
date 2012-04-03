package com.tbocek.android.combatmap.view.interaction;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.BackgroundImage;
import com.tbocek.android.combatmap.model.primitives.BoundingRectangle;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Provides an interaction mode that allows importing and creating images.
 * @author Tim
 *
 */
public class BackgroundImageInteractionMode extends BaseDrawInteractionMode {

	private static final int HANDLE_CIRCLE_RADIUS = 4;
	
	private BackgroundImage mSelectedImage;
	
	/**
	 * Enum for representing what aspect of an image is being dragged.
	 * @author Tim
	 *
	 */
	private enum HandleMode {
		// CHECKSTYLE:OFF
		MOVE,
		UPPER_LEFT,
		LEFT,
		LOWER_LEFT,
		BOTTOM,
		LOWER_RIGHT,
		RIGHT,
		UPPER_RIGHT,
		TOP,
		// CHECKSTYLE:ON
	}
	
	/**
	 * Provides ability to specify a rectangle and query the locations of the
	 * handles to manipulate parts of that rectangle.
	 * @author Tim
	 *
	 */
	private class HandleSet {
		float mXmin;
		float mXmax;
		float mYmin;
		float mYmax;
		
		public HandleSet(float xmin, float xmax, float ymin, float ymax) {
			mXmin = xmin;
			mXmax = xmax;
			mYmin = ymin;
			mYmax = ymax;
		}
		
		public PointF getUpperLeft() {
			return new PointF(mXmin, mYmin);
		}
		
		public PointF getLowerLeft() {
			return new PointF(mXmin, mYmax);
		}
		
		public PointF getUpperRight() {
			return new PointF(mXmax, mYmin);
		}
		
		public PointF getLowerRight() {
			return new PointF(mXmax, mYmax);
		}
		
		public PointF getLeft() {
			return new PointF(mXmin, (mYmin + mYmax) / 2);
		}
		
		public PointF getTop() {
			return new PointF((mXmin + mYmin) / 2, mYmin);
		}
		
		public PointF getRight() {
			return new PointF(mXmax, (mYmin + mYmax) / 2);
		}
		
		public PointF getBottom() {
			return new PointF((mXmin + mYmin) / 2, mYmax);
		}
		
		public HandleMode getHandleMode(PointF p) {
			if (Util.distance(p, getLeft()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.LEFT;
			} else if (Util.distance(p, getRight()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.RIGHT;
			} else if (Util.distance(p, getTop()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.TOP;
			} else if (Util.distance(p, getBottom()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.BOTTOM;
			} else if (Util.distance(p, getUpperLeft()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.UPPER_LEFT;
			} else if (Util.distance(p, getLowerLeft()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.LOWER_LEFT;
			} else if (Util.distance(p, getUpperRight()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.UPPER_RIGHT;
			} else if (Util.distance(p, getLowerRight()) < HANDLE_CIRCLE_RADIUS) {
				return HandleMode.LOWER_RIGHT;
			} else {
				return HandleMode.MOVE;
			}
		}
	}
	
	private HandleMode mHandleMode;
	
	public BackgroundImageInteractionMode(CombatView view) {
		super(view);
	}

	@Override
	public boolean onDown(final MotionEvent e) {
		PointF locationWorldSpace = getView().getData().getWorldSpaceTransformer().screenSpaceToWorldSpace(new PointF(e.getX(), e.getY()));
	
		mSelectedImage = getView().getData().getBackgroundImages().getImageOnPoint(locationWorldSpace);
		
		// Select a handle mode based on what part of the image was touched.
		BoundingRectangle r = mSelectedImage.getBoundingRectangle();
		// Convert bounding rectangle bounds to screen space.
		float xmin = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getXMin());
		float xmax = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getXMax());
		float ymin = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getYMin());
		float ymax = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getYMax());
		
		mHandleMode = new HandleSet(xmin, xmax, ymin, ymax).getHandleMode(new PointF(e.getX(), e.getY()));
		
		getView().refreshMap();
		return true;
	}
	
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
    	PointF locationWorldSpace = getView().getData().getWorldSpaceTransformer().screenSpaceToWorldSpace(new PointF(e.getX(), e.getY()));
    	
    	BackgroundImage tappedImage = getView().getData().getBackgroundImages().getImageOnPoint(locationWorldSpace);
    	
    	if (tappedImage == null) {
	    	BackgroundImage i = new BackgroundImage(getView().getResources().getDrawable(R.drawable.add_image));
	        i.setLocation(locationWorldSpace);
	    	getView().getData().getBackgroundImages().addImage(i);
	    	getView().refreshMap();
    	}
        return true;
    }
    
    @Override
    public void draw(Canvas c) {
    	// Draw border and handles on the selected image.
    	if (mSelectedImage != null) {
    		BoundingRectangle r = mSelectedImage.getBoundingRectangle();
    		// Convert bounding rectangle bounds to screen space.
    		float xmin = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getXMin());
    		float xmax = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getXMax());
    		float ymin = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getYMin());
    		float ymax = getView().getData().getWorldSpaceTransformer().worldSpaceToScreenSpace(r.getYMax());
	    	Paint borderHandlePaint = new Paint();
	    	borderHandlePaint.setColor(Util.ICS_BLUE);
	    	borderHandlePaint.setStrokeWidth(2);
	    	
	    	// TODO: add border segments such that they don't overlap the circles.
	    	
	    	HandleSet handles = new HandleSet(xmin, xmax, ymin, ymax);
	    	drawHandle(c, handles.getLeft(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getLeft(), handles.getUpperLeft(), borderHandlePaint);
	    	drawHandle(c, handles.getUpperLeft(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getUpperLeft(), handles.getTop(), borderHandlePaint);
	    	drawHandle(c, handles.getTop(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getTop(), handles.getUpperRight(), borderHandlePaint);
	    	drawHandle(c, handles.getUpperRight(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getUpperRight(), handles.getRight(), borderHandlePaint);
	    	drawHandle(c, handles.getRight(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getRight(), handles.getLowerRight(), borderHandlePaint);
	    	drawHandle(c, handles.getLowerRight(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getLowerRight(), handles.getBottom(), borderHandlePaint);
	    	drawHandle(c, handles.getBottom(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getBottom(), handles.getLowerLeft(), borderHandlePaint);
	    	drawHandle(c, handles.getLowerLeft(), borderHandlePaint);
	    	drawBorderSegment(c, handles.getLowerLeft(), handles.getLeft(), borderHandlePaint);
	    	
    	}
    }
    
    private void drawHandle(Canvas c, PointF location, Paint p) {
    	c.drawCircle(location.x, location.y, HANDLE_CIRCLE_RADIUS, p);
    }
    
    /**
     * Helper function to draw a segment of the selection border.  Accounts
     * for the presence of a circle handle and doesn't overlap it.
     * @param c
     * @param p1
     * @param p2
     * @param p
     */
    private void drawBorderSegment(Canvas c, PointF p1, PointF p2, Paint p) {
    	float horizontalClip = Math.abs(p1.x - p2.x) > Math.abs(p1.y - p2.y) ? HANDLE_CIRCLE_RADIUS : 0;
    	float verticalClip = Math.abs(p1.x - p2.x) < Math.abs(p1.y - p2.y) ? HANDLE_CIRCLE_RADIUS : 0;
    	
    	c.drawLine(
    			Math.min(p1.x, p2.x) + horizontalClip, 
    			Math.min(p1.y, p2.y) + verticalClip,
    			Math.max(p1.x, p2.x) - horizontalClip, 
    			Math.max(p1.y, p2.y) - verticalClip,
    			p);
    }
}
