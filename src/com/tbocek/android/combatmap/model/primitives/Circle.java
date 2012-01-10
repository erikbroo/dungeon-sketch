package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;


import android.graphics.Path;

public class Circle extends Shape implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3662381313335509811L;

	public static final String SHAPE_TYPE = "cr";

	// In world space.
	PointF mCenter = null;
	float mRadius = Float.NaN;

	/**
	 * A point on the edge of the circle, where the user first placed his
	 * finger.
	 */
	PointF startPoint = null;

	FreehandLine lineForErasing = null;

	public Circle(int color, float newLineStrokeWidth) {
        this.mColor = color;
        this.mWidth = newLineStrokeWidth;
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
		return lineForErasing != null;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> l = new ArrayList<Shape>();
		l.add(lineForErasing);
		lineForErasing = null;
		invalidatePath();
		return l;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (lineForErasing != null) {
			lineForErasing.erase(center, radius);
			invalidatePath();
			return;
		}

		if (!boundingRectangle.intersectsWithCircle(center, radius)) {
			return;
		}

		float d = Util.distance(this.mCenter, center);

		if (d <= radius + this.mRadius 
				&& d >= Math.abs(radius - this.mRadius)) {
			createLineForErasing();
			lineForErasing.erase(center, radius);
			invalidatePath();
		}
	}

	private void createLineForErasing() {
		lineForErasing = new FreehandLine(this.mColor, this.mWidth);
		for (float rad = 0; rad < 2 * Math.PI; rad += 2 * Math.PI / 64f) {
			lineForErasing.addPoint(new PointF(
					(float) mCenter.x + mRadius * (float) Math.cos(rad),
					(float) mCenter.y + mRadius * (float) Math.sin(rad)));
		}
	}

	@Override
	protected Path createPath() {
		if (mCenter == null || mRadius == Float.NaN) {
			return null;
		}

		if (lineForErasing != null) {
			return lineForErasing.createPath();
		} else {
			Path p = new Path();
			p.addCircle(mCenter.x, mCenter.y, mRadius, Path.Direction.CW);
			return p;
		}
	}

	@Override
	public void addPoint(PointF p) {
		if (startPoint == null) {
			startPoint = p;
		} else {
			// Create a circle where the line from startPoint to P is a
			// diameter.
			mRadius = Util.distance(startPoint, p) / 2;
			mCenter = new PointF(
					(p.x + startPoint.x) / 2, (p.y + startPoint.y) / 2);
			invalidatePath();
			boundingRectangle = new BoundingRectangle();
			boundingRectangle.updateBounds(
					new PointF(mCenter.x - mRadius, mCenter.y - mRadius));
			boundingRectangle.updateBounds(
					new PointF(mCenter.x + mRadius, mCenter.y + mRadius));
		}
	}
	
	public boolean shouldSerialize() {
		return this.mRadius == this.mRadius && this.mCenter != null;
	}
	
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
