package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.List;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region.Op;

/**
 * Abstract base class representing a shape.
 * @author Tim
 *
 */
public abstract class Shape {

	/**
	 * Value in [0, 255] to use for the alpha channel when drawing shapes as
	 * fog of war regions.
	 */
	private static final int FOG_OF_WAR_ALPHA = 128;
	
	/**
	 * Paint object that is used when drawing fog of war regions for the fog
	 * of war editor.
	 */
	private static Paint fogOfWarPaint;
	
	/**
	 * Deserializes and returns a shape.
	 * @param s The stream to read from.
	 * @return The created shape.
	 * @throws IOException On deserialization error.
	 */
	public static Shape deserialize(MapDataDeserializer s) throws IOException {
		String shapeType = s.readString();
		s.expectObjectStart();
		int color = s.readInt();
		float width = s.readFloat();
		BoundingRectangle r = BoundingRectangle.deserialize(s);
		s.expectObjectEnd();
		
		Shape shape;
		
		if (shapeType.equals(FreehandLine.SHAPE_TYPE)) {
			shape = new FreehandLine(color, width);
		} else if (shapeType.equals(StraightLine.SHAPE_TYPE)) {
			shape = new StraightLine(color, width);
		} else if (shapeType.equals(Circle.SHAPE_TYPE)) {
			shape = new Circle(color, width);
		} else if (shapeType.equals(Text.SHAPE_TYPE)) {
			shape = new Text(color, width);
		} else if (shapeType.equals(Rectangle.SHAPE_TYPE)) {
			shape = new Rectangle(color, width);
		} else {
			throw new IOException("Unrecognized shape type: " + shapeType);
		}
		
		shape.mBoundingRectangle = r;
		shape.shapeSpecificDeserialize(s);
		return shape;
	}
	
	/**
	 * The paint object that will be used to draw this line.
	 */
	private transient Paint mPaint;
	
	/**
	 * X component of the pending move operation.
	 */
	private float mDrawOffsetDeltaX = Float.NaN;
	
	/**
	 * Y component of the pending move operation.
	 */
	private float mDrawOffsetDeltaY = Float.NaN;
	

	/**
	 * Cached path that represents this line.
	 */
	private transient Path mPath;
	
	/**
	 * The color to draw this line with.
	 */
	private int mColor = Color.BLACK;
	
	/**
	 * The stroke width to draw this line with.  +Infinity will use a fill
	 * instead (to ensure that it draws beneath all lines).
	 */
	private float mWidth;
	
	/**
	 * Cached rectangle that bounds all the points in this line.
	 * This could be computed on demand, but it is easy enough to update
	 * every time a point is added.
	 */
	private BoundingRectangle mBoundingRectangle = new BoundingRectangle();

	/**
	 * Checks whether this shape contains the given point.
	 * @param p The point to check.
	 * @return True if that point falls within this shape.
	 */
	public abstract boolean contains(PointF p);

	/**
	 * @return True if this shape can be optimized.
	 */
	public abstract boolean needsOptimization();

	/**
	 * Optimizes this shape by removing erased points.
	 * @return A list of shapes that this shape optimizes to, since removing
	 * 		erased points may create disjoint line segments.
	 */
	public abstract List<Shape> removeErasedPoints();

	/**
	 * Erases the portion of this shape that falls within the given circle.
	 * @param center Center of the circle.
	 * @param radius Radius of the circle.
	 */
	public abstract void erase(final PointF center, final float radius);

	/**
	 * Creates the Android graphics Path object used to draw this shape.
	 * @return The created path.
	 */
	protected abstract Path createPath();

	/**
	 * Adds a point to this shape.  This is used when dragging, so depending on 
	 * implementation, this may either add a point or may modify the 
	 * size/position of the shape.
	 * @param p The point to add.
	 */
	public abstract void addPoint(final PointF p);
	
	/**
	 * Sets a temporary offset for drawing this shape, which can be thought of
	 * as a pending move operation.  This will cause the shape to change the 
	 * tranformation until the operation is committed, which wipes the offset 
	 * data and returns a copy of the shape that is permanently modified with 
	 * the new offset.  We do not directly modify this shape so that we can
	 * support undo/redo.
	 * @param deltaX Amount to move the shape in X dimension.
	 * @param deltaY Amount to move the shape in Y dimension.
	 */
	public void setDrawOffset(float deltaX, float deltaY) {
		mDrawOffsetDeltaX = deltaX;
		mDrawOffsetDeltaY = deltaY;
	}
	
	/**
	 * Changes the given canvas's transformation to apply this draw offset.
	 * @param c The canvas to modify.
	 */
	public void applyDrawOffsetToCanvas(Canvas c) {
		if (hasOffset()) {
			c.save();
			c.translate(mDrawOffsetDeltaX, mDrawOffsetDeltaY);
		}
	}
	
	/**
	 * Changes the given canvas's transformation to remove this draw offset.
	 * @param c The canvas to modify.
	 */
	public void revertDrawOffsetFromCanvas(Canvas c) {
		if (hasOffset()) {
			c.restore();
		}
	}
	
	/**
	 * 
	 * @return Whether this shape has a temporary pending move operation.
	 */
	public boolean hasOffset() {
		return mDrawOffsetDeltaX == mDrawOffsetDeltaX;
	}
	
	/**
	 * Removes the pending move operation.
	 */
	private void clearDrawOffset() {
		mDrawOffsetDeltaX = Float.NaN;
		mDrawOffsetDeltaY = Float.NaN;
	}
	
	/**
	 * Commits the pending move operation by returning a copy of this shape with
	 * the offset applied.  The offset is cleared from this shape.  Calling code
	 * should set up the proper undo/redo operation to actually implement the
	 * move.
	 * @return Moved copy of the shape.
	 */
	public Shape commitDrawOffset() {
		if (!hasOffset()) {
			return null;
		}
		
		Shape s = getMovedShape(mDrawOffsetDeltaX, mDrawOffsetDeltaY);
		clearDrawOffset();
		return s;
	}
	
	/**
	 * 
	 * @param deltaX Amount to move by in x dimension.
	 * @param deltaY Amount to move by in Y dimension.
	 * @return A *copy* of this shape that is moved by the given offset in
	 * 		world space.
	 */
	protected Shape getMovedShape(float deltaX, float deltaY) {
		//TODO: Implement this for each subclass, and make this abstract.
		throw new RuntimeException(
				"This shape does not support the move operation.");
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
	    	c.drawPath(mPath, mPaint);
	    }
	}

	/**
	 * Draws this path specifically as a fog of war region.
	 * @param c Canvas to draw on.
	 */
	public void drawFogOfWar(final Canvas c) {
		// Ensure the static fog of war pen is created.
		if (fogOfWarPaint == null) {
			Paint p = new Paint();
			p.setColor(Color.RED);
			p.setAlpha(FOG_OF_WAR_ALPHA);
			p.setStyle(Paint.Style.FILL);
			fogOfWarPaint = p;
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
			c.clipPath(mPath, Op.UNION);
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
		if (mPaint == null) {
	        mPaint = new Paint();
	        mPaint.setColor(mColor);
	        if (getWidth() == Float.POSITIVE_INFINITY) {
	        	mPaint.setStyle(Paint.Style.FILL);
	        } else {
		        mPaint.setStrokeWidth(getWidth());
		        mPaint.setStyle(Paint.Style.STROKE);
	        }
	    }
	}

	/**
	 * Gets the smallest rectangle needed to fully enclose the line.
	 * @return The bounding rectangle.
	 */
	public BoundingRectangle getBoundingRectangle() {
	    return mBoundingRectangle;
	}

	/**
	 * @return This line's stroke width.
	 */
	public float getStrokeWidth() {
	    return this.getWidth();
	}

	/**
	 * @return True if this should be drawn below the grid based on its size,
	 * 		false otherwise.
	 * @return
	 */
	public boolean shouldDrawBelowGrid() {
		return this.getWidth() > 1.0f;
	}
	
	/**
	 * Serializes this shape to the given stream.  Must call serializeBase()
	 * @param s The stream to serialize to.
	 * @throws IOException On serialization error.
	 */
	public abstract void serialize(MapDataSerializer s) throws IOException;

	/**
	 * Serializes shared attributes from the Shape base class.
	 * @param s The shape to serialize.
	 * @param shapeType Tag indicating the type of shape being serialized.
	 * @throws IOException On serialization error.
	 */
	protected void serializeBase(MapDataSerializer s, String shapeType) 
			throws IOException {
		s.serializeString(shapeType);
		s.startObject();
		s.serializeInt(this.mColor);
		s.serializeFloat(this.getWidth());
		this.mBoundingRectangle.serialize(s);
		s.endObject();
	}

	/**
	 * Template method that loads shape-specific data from the deserialization
	 * stream.
	 * @param s Stream to read from.
	 * @throws IOException On deserialization error.
	 */
	protected abstract void shapeSpecificDeserialize(MapDataDeserializer s)
			throws IOException;

	/**
	 * @return This shape's color.
	 */
	public int getColor() {
		return mColor;
	}

	/**
	 * Sets the current shape's color.
	 * @param color The new color.
	 */
	public void setColor(int color) {
		mColor = color;
	}

	/**
	 * @return This shape's line width
	 */
	public float getWidth() {
		return mWidth;
	}

	/**
	 * Sets the width of the current line.
	 * @param width The line width.
	 */
	public void setWidth(float width) {
		mWidth = width;
	}
	
	/** 
	 * @return The paint object that should be used to draw this shape.
	 */
	protected Paint getPaint() {
		ensurePaintCreated();
		return mPaint;
	}
    
    /**
     * Whether the shape is in a valid state.  Subclasses should override this
     * with their own checks.  If returns false, the shape may be:
     * - Removed from the line collection at any time.
     * - Stopped from serializing.
     * @return True if the shape is in a valid state, False otherwise.
     */
    public boolean isValid() {
    	return true;
    }

}