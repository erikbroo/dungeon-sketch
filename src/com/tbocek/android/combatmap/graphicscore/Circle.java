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
		if (center == null) return false;
		return Util.distance(p, center) < radius;
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

		float d = Util.distance(this.center, center);

		if ( d <= radius + this.radius && d >= Math.abs(radius - this.radius)) {
			createLineForErasing();
			lineForErasing.erase(center, radius);
			invalidatePath();
		}
	}

	private void createLineForErasing() {
		lineForErasing = new FreehandLine(this.mColor, this.mWidth);
		for (float rad = 0; rad < 2 * Math.PI; rad += 2 * Math.PI / 64f) {
			lineForErasing.addPoint(new PointF(
					(float) center.x + radius * (float) Math.cos(rad),
					(float) center.y + radius * (float) Math.sin(rad)));
		}
	}

	@Override
	protected Path createPath() {
		if (center == null || radius == Float.NaN) {
			return null;
		}

		if (lineForErasing != null) {
			return lineForErasing.createPath();
		} else {
			Path p = new Path();
			p.addCircle(center.x, center.y, radius, Path.Direction.CW);
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
