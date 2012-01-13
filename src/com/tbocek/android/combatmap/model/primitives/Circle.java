package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;


import android.graphics.Path;

/**
 * Represents a circle drawn on the map.
 * @author Tim
 *
 */
public class Circle extends Shape {

	/**
	 * Short character string that is the type of the shape.
	 */
	public static final String SHAPE_TYPE = "cr";
	
	/**
	 * When converting the circle into a freehand line that is a polygon to
	 * approximate a circle, the number of line segments to use.
	 */
	private static final int FREEHAND_LINE_CONVERSION_SEGMENTS = 64;

	/**
	 * Center of the circle, in world space.
	 */
	private PointF mCenter;
	
	/**
	 * Radius of the circle, in world space.
	 */
	private float mRadius;

	/**
	 * A point on the edge of the circle, where the user first placed his
	 * finger.
	 */
	private PointF mStartPoint;

	/**
	 * When the user starts erasing a circle, it is converted into a freehand
	 * line for easier erasing.
	 */
	private FreehandLine mLineForErasing;

	/**
	 * Constructor from line properties.  Center and radius to be set later.
	 * @param color Color of the new circle.
	 * @param newLineStrokeWidth Stroke width of the new circle.
	 */
	public Circle(int color, float newLineStrokeWidth) {
        this.setColor(color);
        this.setWidth(newLineStrokeWidth);
	}

	@Override
	public boolean contains(PointF p) {
		if (mCenter == null) {
			return false;
		}
		return Util.distance(p, mCenter) < mRadius;
	}

	@Override
	public boolean needsOptimization() {
		return mLineForErasing != null;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> l = new ArrayList<Shape>();
		l.add(mLineForErasing);
		mLineForErasing = null;
		invalidatePath();
		return l;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (mLineForErasing != null) {
			mLineForErasing.erase(center, radius);
			invalidatePath();
			return;
		}

		if (!getBoundingRectangle().intersectsWithCircle(center, radius)) {
			return;
		}

		float d = Util.distance(this.mCenter, center);

		if (d <= radius + this.mRadius 
				&& d >= Math.abs(radius - this.mRadius)) {
			createLineForErasing();
			mLineForErasing.erase(center, radius);
			invalidatePath();
		}
	}

	/**
	 * Converts this circle into a freehand line that approximates a circle, so
	 * that we can then erase segments of the freehand line.
	 */
	private void createLineForErasing() {
		mLineForErasing = new FreehandLine(this.getColor(), this.getWidth());
		for (float rad = 0; rad < 2 * Math.PI; 
				rad += 2 * Math.PI / FREEHAND_LINE_CONVERSION_SEGMENTS) {
			mLineForErasing.addPoint(new PointF(
					mCenter.x + mRadius * (float) Math.cos(rad),
					mCenter.y + mRadius * (float) Math.sin(rad)));
		}
	}

	@Override
	protected Path createPath() {
		if (mCenter == null || mRadius == Float.NaN) {
			return null;
		}

		if (mLineForErasing != null) {
			return mLineForErasing.createPath();
		} else {
			Path p = new Path();
			p.addCircle(mCenter.x, mCenter.y, mRadius, Path.Direction.CW);
			return p;
		}
	}

	@Override
	public void addPoint(PointF p) {
		if (mStartPoint == null) {
			mStartPoint = p;
		} else {
			// Create a circle where the line from startPoint to P is a
			// diameter.
			mRadius = Util.distance(mStartPoint, p) / 2;
			mCenter = new PointF(
					(p.x + mStartPoint.x) / 2, (p.y + mStartPoint.y) / 2);
			invalidatePath();
			getBoundingRectangle().clear();
			getBoundingRectangle().updateBounds(
					new PointF(mCenter.x - mRadius, mCenter.y - mRadius));
			getBoundingRectangle().updateBounds(
					new PointF(mCenter.x + mRadius, mCenter.y + mRadius));
		}
	}
	

	@Override
	public boolean shouldSerialize() {
		return this.mRadius == this.mRadius && this.mCenter != null;
	}

	@Override
    public void serialize(MapDataSerializer s) throws IOException {
    	serializeBase(s, SHAPE_TYPE);
    	s.startObject();
    	s.serializeFloat(this.mRadius);
    	s.serializeFloat(this.mCenter.x);
    	s.serializeFloat(this.mCenter.y);
    	s.endObject();
    }

	@Override
	protected void shapeSpecificDeserialize(MapDataDeserializer s)
			throws IOException {
		s.expectObjectStart();
		this.mRadius = s.readFloat();
		mCenter = new PointF();
		this.mCenter.x = s.readFloat();
		this.mCenter.y = s.readFloat();
		s.expectObjectEnd();
	}
}
