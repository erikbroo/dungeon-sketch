package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Path;

public class StraightLine extends Shape implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7125928496175113220L;

	private boolean erased = false;
	private PointF start = null;
	private PointF end = null;

	public StraightLine(int color, float newLineStrokeWidth) {
        this.mColor = color;
        this.mWidth = newLineStrokeWidth;
	}

	@Override
	public boolean contains(PointF p) {
		// Cannot define a region.
		return false;
	}

	@Override
	public boolean needsOptimization() {
		return erased;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> shapes = new ArrayList<Shape>();
		if (!erased) {
			shapes.add(this);
		}
		return shapes;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (start == null || end == null) {
			return;
		}
		// Special case - if we have only two points, this is probably
		// a large straight line and we want to erase the line if the
		// eraser intersects with it.  However, this is an expensive
		// test, so we don't want to do it for all line segments when
		// they are generally small enough for the eraser to enclose.
		// Formula from:
		// http://mathworld.wolfram.com/Circle-LineIntersection.html
		PointF p1 = start;
		PointF p2 = end;

		// Transform to standard coordinate system where circle is
		// centered at (0, 0)
		float x1 = p1.x - center.x;
		float y1 = p1.y - center.y;
		float x2 = p2.x - center.x;
		float y2 = p2.y - center.y;

		float dsquared = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		float det = x1 * y2 - x2 * y1;
		float discriminant = radius * radius * dsquared - det * det;

		// Intersection if the discriminant is non-negative.  In this
		// case we delete the entire line.
		if (discriminant >= 0) {
			erased = true;
			invalidatePath();
		}
	}

	@Override
	protected Path createPath() {
		if (start == null || end == null || erased) {
			return null;
		}
		Path path = new Path();
		path.moveTo(start.x, start.y);
		path.lineTo(end.x, end.y);
		return path;
	}

	@Override
	public void addPoint(PointF p) {
		if (start == null) {
			start = p;
		} else {
			end = p;
			// Re-create the bounding rectangle every time this is done.
			boundingRectangle = new BoundingRectangle();
			boundingRectangle.updateBounds(start);
			boundingRectangle.updateBounds(end);
			invalidatePath();
		}

	}

}
