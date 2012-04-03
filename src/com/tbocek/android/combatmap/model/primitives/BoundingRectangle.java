package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.Collection;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

import android.graphics.RectF;

/**
 * Represents a recangle that bounds a number of drawable Dungeon Sketch
 * objects.
 * @author Tim
 *
 */
public final class BoundingRectangle {
	
	/**
	 * Constructs a bounding rectangle that uses the given points as bounds.
	 * @param p1 First point.
	 * @param p2 Second point.
	 */
	public BoundingRectangle(PointF p1, PointF p2) {
		mXMin = p1.x;
		mXMax = p1.x;
		mYMin = p1.y;
		mYMax = p1.y;
		updateBounds(p2);
	}
	
	/**
	 * Default ctor.
	 */
	public BoundingRectangle() {}

	/**
	 * Creates a new BoundingRectangle by reading from the given stream.
	 * @param s The deserialization object to load from.
	 * @return The loaded BoundingRectangle.
	 * @throws IOException On deserialization error.
	 */
	public static BoundingRectangle deserialize(MapDataDeserializer s)
			throws IOException {
		BoundingRectangle r = new BoundingRectangle();
		r.mXMin = s.readFloat();
		r.mXMax = s.readFloat();
		r.mYMin = s.readFloat();
		r.mYMax = s.readFloat();
		return r;
	}
	
    /**
     * Current left bounds.
     */
    private float mXMin = Float.MAX_VALUE;

    /**
     * Current right bounds.
     */
    private float mXMax = Float.MIN_NORMAL;

    /**
     * Current top bounds.
     */
    private float mYMin = Float.MAX_VALUE;

    /**
     * Current bottom bounds.
     */
    private float mYMax = Float.MIN_NORMAL;

    /**
     * @return The left bounds.
     */
    public float getXMin() {
        return mXMin;
    }

    /**
     * @return The right bounds.
     */
    public float getXMax() {
        return mXMax;
    }

    /**
     * @return The top bounds.
     */
    public float getYMin() {
        return mYMin;
    }

    /**
     * @return The bottom bounds.
     */
    public float getYMax() {
        return mYMax;
    }

    /**
     * @return The width.
     */
    public float getWidth() {
        return mXMax - mXMin;
    }

    /**
     * @return The height.
     */
    public float getHeight() {
        return mYMax - mYMin;
    }

    /**
     * Updates the bounds of the rectangle so that the given point is also
     * included.
     * @param p The point to include.
     */
    public void updateBounds(final PointF p) {
        this.mXMin = Math.min(this.mXMin, p.x);
        this.mXMax = Math.max(this.mXMax, p.x);
        this.mYMin = Math.min(this.mYMin, p.y);
        this.mYMax = Math.max(this.mYMax, p.y);
    }

    /**
     * Updates the bounds for an entire collection of points.
     * @param points The points to update with.
     */
	public void updateBounds(final Collection<PointF> points) {
		for (PointF p : points) {
			updateBounds(p);
		}
	}

    /**
     * Updates the bounds of the rectangle so that the given rectangle is fully
     * included as well.
     * @param other Other bounding rectangle to include.
     */
    public void updateBounds(final BoundingRectangle other) {
        this.mXMin = Math.min(this.mXMin, other.mXMin);
        this.mXMax = Math.max(this.mXMax, other.mXMax);
        this.mYMin = Math.min(this.mYMin, other.mYMin);
        this.mYMax = Math.max(this.mYMax, other.mYMax);
    }

    /**
     * Checks if this bounding rectangle contains a point.
     * @param p The point to check.
     * @return True if the point lies within this rectangle.
     */
    public boolean contains(final PointF p) {
        return p.x >= mXMin && p.x <= mXMax
            && p.y >= mYMin && p.y <= mYMax;
    }

    /**
     * Checks whether this bounding rectangle contains part of a circle.
     * (Note that this is an approximation and checks the circle's bounding
     * rectangle, as intersecting a line and a circle is a much slower
     * operation).
     * @param center The center of the circle to check.
     * @param radius The radius of the circle to check.
     * @return True if the bounding rectangle contains part of this circle.
     */
    public boolean intersectsWithCircle(
            final PointF center, final float radius) {
        return center.x + radius >= mXMin
            && center.x - radius <= mXMax
            && center.y + radius >= mYMin
            && center.y - radius <= mYMax;
    }

    /**
     * Converts to as Android RectF.
     * @return The RectF.
     */
	public RectF toRectF() {
		return new RectF(mXMin, mYMin, mXMax, mYMax);
	}

	/**
	 * Translates the rectangle in place by the specified amount.
	 * @param deltaX Amount to move in X direction.
	 * @param deltaY Amount to move in Y direction.
	 */
	public void move(float deltaX, float deltaY) {
		mXMin += deltaX;
		mXMax += deltaX;
		mYMin += deltaY;
		mYMax += deltaY;
	}
	
	/**
	 * Saves the rectangle to the given serialization stream.
	 * @param s Serialization object to save to.
	 * @throws IOException On serialization error.
	 */
	public void serialize(MapDataSerializer s) throws IOException {
		s.serializeFloat(mXMin);
		s.serializeFloat(mXMax);
		s.serializeFloat(mYMin);
		s.serializeFloat(mYMax);
	}

	/**
	 * Returns the bounding rectangle to its initial state.
	 */
	public void clear() {
		   mXMin = Float.MAX_VALUE;
		   mXMax = Float.MIN_NORMAL;
		   mYMin = Float.MAX_VALUE;
		   mYMax = Float.MIN_NORMAL;
	}

}