package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Path;

public class Circle extends Shape implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3662381313335509811L;

	// In world space.
	PointF center = null;
	float radius = Float.NaN;
	boolean erased = false;

	/**
	 * A point on the edge of the circle, where the user first placed his
	 * finger.
	 */
	PointF startPoint = null;

	public Circle(int color, float newLineStrokeWidth) {
        this.mColor = color;
        this.mWidth = newLineStrokeWidth;
	}

	@Override
	public boolean contains(PointF p) {
		if (center == null) return false;
		return Util.distance(p, center) < radius;
	}

	@Override
	public boolean needsOptimization() {
		return erased || center == null;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> shapes = new ArrayList<Shape>();
		if (!erased && center != null) {
			shapes.add(this);
		}
		erased = false;
		return shapes;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (!boundingRectangle.intersectsWithCircle(center, radius)) {
			return;
		}

		float d = Util.distance(this.center, center);

		if ( d <= radius + this.radius && d >= Math.abs(radius - this.radius)) {
			erased = true;
			invalidatePath();
		}
	}

	@Override
	protected Path createPath() {
		if (erased || center == null || radius == Float.NaN) {
			return null;
		}

		Path p = new Path();
		p.addCircle(center.x, center.y, radius, Path.Direction.CW);
		return p;
	}

	@Override
	public void addPoint(PointF p) {
		if (startPoint == null) {
			startPoint = p;
		} else {
			// Create a circle where the line from startPoint to P is a
			// diameter.
			radius = Util.distance(startPoint, p) / 2;
			center = new PointF(
					(p.x + startPoint.x) / 2, (p.y + startPoint.y) / 2);
			invalidatePath();
			boundingRectangle = new BoundingRectangle();
			boundingRectangle.updateBounds(
					new PointF(center.x - radius, center.y - radius));
			boundingRectangle.updateBounds(
					new PointF(center.x + radius, center.y + radius));
		}
	}

}
