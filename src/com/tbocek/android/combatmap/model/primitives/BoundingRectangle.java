package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.io.Serializable;
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
public final class BoundingRectangle implements Serializable {
    /**
     * ID for serialization.
     */
    private static final long serialVersionUID = 2063166112708845928L;

    /**
     * Current left bounds.
     */
    private float boundsXMin = Float.MAX_VALUE;

    /**
     * Current right bounds.
     */
    private float boundsXMax = Float.MIN_VALUE;

    /**
     * Current top bounds.
     */
    private float boundsYMin = Float.MAX_VALUE;

    /**
     * Current bottom bounds.
     */
    private float boundsYMax = Float.MIN_VALUE;

    /**
     * @return The left bounds.
     */
    public float getXMin() {
        return boundsXMin;
    }

    /**
     * @return The right bounds.
     */
    public float getXMax() {
        return boundsXMax;
    }

    /**
     * @return The top bounds.
     */
    public float getYMin() {
        return boundsYMin;
    }

    /**
     * @return The bottom bounds.
     */
    public float getYMax() {
        return boundsYMax;
    }

    /**
     * @return The width.
     */
    public float getWidth() {
        return boundsXMax - boundsXMin;
    }

    /**
     * @return The height.
     */
    public float getHeight() {
        return boundsYMax - boundsYMin;
    }

    /**
     * Updates the bounds of the rectangle so that the given point is also
     * included.
     * @param p The point to include.
     */
    public void updateBounds(final PointF p) {
        this.boundsXMin = Math.min(this.boundsXMin, p.x);
        this.boundsXMax = Math.max(this.boundsXMax, p.x);
        this.boundsYMin = Math.min(this.boundsYMin, p.y);
        this.boundsYMax = Math.max(this.boundsYMax, p.y);
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
        this.boundsXMin = Math.min(this.boundsXMin, other.boundsXMin);
        this.boundsXMax = Math.max(this.boundsXMax, other.boundsXMax);
        this.boundsYMin = Math.min(this.boundsYMin, other.boundsYMin);
        this.boundsYMax = Math.max(this.boundsYMax, other.boundsYMax);
    }

    /**
     * Checks if this bounding rectangle contains a point.
     * @param p The point to check.
     * @return True if the point lies within this rectangle.
     */
    public boolean contains(final PointF p) {
        return p.x >= boundsXMin && p.x <= boundsXMax
            && p.y >= boundsYMin && p.y <= boundsYMax;
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
        return center.x + radius >= boundsXMin
            && center.x - radius <= boundsXMax
            && center.y + radius >= boundsYMin
            && center.y - radius <= boundsYMax;
    }

	public RectF toRectF() {
		return new RectF(boundsXMin, boundsYMin, boundsXMax, boundsYMax);
	}

	public void move(float deltaX, float deltaY) {
		boundsXMin += deltaX;
		boundsXMax += deltaX;
		boundsYMin += deltaY;
		boundsYMax += deltaY;
	}
	
	public void serialize(MapDataSerializer s) throws IOException {
		s.serializeFloat(boundsXMin);
		s.serializeFloat(boundsXMax);
		s.serializeFloat(boundsYMin);
		s.serializeFloat(boundsYMax);
	}
	
	public static BoundingRectangle deserialize(MapDataDeserializer s)
			throws IOException {
		BoundingRectangle r = new BoundingRectangle();
		r.boundsXMin = s.readFloat();
		r.boundsXMax = s.readFloat();
		r.boundsYMin = s.readFloat();
		r.boundsYMax = s.readFloat();
		return r;
	}
}