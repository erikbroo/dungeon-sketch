package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region.Op;

public abstract class Shape implements Serializable {

	/**
	 * ID for serialization.
	 */
	private static final long serialVersionUID = -7280375520671521194L;

	/**
	 * The paint object that will be used to draw this line.
	 */
	protected transient Paint paint;

	public abstract boolean contains(PointF p);

	public abstract boolean needsOptimization();

	public abstract List<Shape> removeErasedPoints();

	public abstract void erase(final PointF center, final float radius);

	protected abstract Path createPath();

	public abstract void addPoint(final PointF p);
	

	private float drawOffsetDeltaX = Float.NaN;
	private float drawOffsetDeltaY = Float.NaN;
	
	/**
	 * Sets a temporary offset for drawing this shape.  This will cause the
	 * shape to change the tranformation until the operation is committed, which
	 * wipes the offset data and returns a copy of the shape that is permanently
	 * modified with the new offset.
	 * @param deltaX
	 * @param deltaY
	 */
	public void setDrawOffset(float deltaX, float deltaY) {
		drawOffsetDeltaX = deltaX;
		drawOffsetDeltaY = deltaY;
	}
	
	public void applyDrawOffsetToCanvas(Canvas c) {
		if (hasOffset()) {
			c.save();
			c.translate(drawOffsetDeltaX, drawOffsetDeltaY);
		}
	}
	
	public void revertDrawOffsetFromCanvas(Canvas c) {
		if (hasOffset()) {
			c.restore();
		}
	}
	
	public boolean hasOffset() {
		// NaN check.
		return drawOffsetDeltaX == drawOffsetDeltaX;
	}
	
	private void clearDrawOffset() {
		drawOffsetDeltaX = Float.NaN;
		drawOffsetDeltaY = Float.NaN;
	}
	
	public Shape commitDrawOffset() {
		//TODO: Roll this into the Commit operation.
		if (drawOffsetDeltaX == Float.NaN) return null;
		
		Shape s = getMovedShape(drawOffsetDeltaX, drawOffsetDeltaY);
		clearDrawOffset();
		return s;
	}
	
	/**
	 * 
	 * @param deltaX
	 * @param deltaY
	 * @return A *copy* of this shape that is moved by the given offset in
	 * 		world space.
	 */
	protected Shape getMovedShape(float deltaX, float deltaY) {
		//TODO: Implement this for each subclass, and make this abstract.
		throw new RuntimeException(
				"This shape does not support the move operation.");
	}

	/**
	 * Cached path that represents this line.
	 */
	private transient Path mPath = null;
	/**
	 * The color to draw this line with.
	 */
	protected int mColor = Color.BLACK;
	/**
	 * The stroke width to draw this line with.  +Infinity will use a fill
	 * instead (to ensure that it draws beneath all lines).
	 */
	protected float mWidth = 2;
	/**
	 * Cached rectangle that bounds all the points in this line.
	 * This could be computed on demand, but it is easy enough to update
	 * every time a point is added.
	 */
	protected BoundingRectangle boundingRectangle = new BoundingRectangle();
	/**
	 * Paint object that is used when drawing fog of war regions for the fog
	 * of war editor.
	 */
	private static Paint fogOfWarPaint = null;

	public Shape() {
		super();
	}

	/**
	 * Invalidates the path so that it is recreated on the next draw operation.
	 */
	protected void invalidatePath() {
		this.mPath = null;
	}

	/**
	 * Draws the line on the given canvas.
	 * @param c Canvas to draw on.
	 */
	public void draw(final Canvas c) {
	    ensurePaintCreated();
	    ensurePathCreated();
	    if (mPath != null) {
	    	c.drawPath(mPath, paint);
	    }
	}

	/**
	 * Draws this path specifically as a fog of war region.
	 * @param c Canvas to draw on.
	 */
	public void drawFogOfWar(final Canvas c) {
		// Ensure the static fog of war pen is created.
		if (fogOfWarPaint == null) {
			fogOfWarPaint = new Paint();
			fogOfWarPaint.setColor(Color.RED);
			fogOfWarPaint.setAlpha(128);
			fogOfWarPaint.setStyle(Paint.Style.FILL);
		}

		ensurePathCreated();
	    if (mPath != null) {
	    	c.drawPath(mPath, fogOfWarPaint);
	    }
	}

	/**
	 * Clips out the region defined by this path on the fog of war.
	 * @param c Canvas to draw on.
	 */
	public void clipFogOfWar(final Canvas c) {
		ensurePathCreated();
		if (mPath != null) {
			c.clipPath(mPath, Op.DIFFERENCE);
		}
	}

	/**
	 * Creates the path if it is currently invalid.
	 */
	private void ensurePathCreated() {
		if (mPath == null) {
	        mPath = createPath();
	    }
	}

	/**
	 * If there is no Paint object cached for this line, create one and set
	 * the appropriate color and stroke width.
	 */
	protected void ensurePaintCreated() {
		if (paint == null) {
	        paint = new Paint();
	        paint.setColor(mColor);
	        if (mWidth == Float.POSITIVE_INFINITY) {
	        	paint.setStyle(Paint.Style.FILL);
	        } else {
		        paint.setStrokeWidth(mWidth);
		        paint.setStyle(Paint.Style.STROKE);
	        }
	    }
	}

	/**
	 * Gets the smallest rectangle needed to fully enclose the line.
	 * @return The bounding rectangle.
	 */
	public BoundingRectangle getBoundingRectangle() {
	    return boundingRectangle;
	}

	/**
	 * @return This line's stroke width.
	 */
	public float getStrokeWidth() {
	    return this.mWidth;
	}

	/**
	 * @return True if this should be drawn below the grid based on its size,
	 * 		false otherwise.
	 * @return
	 */
	public boolean shouldDrawBelowGrid() {
		return this.mWidth > 1.0f;
	}


}