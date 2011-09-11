package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.graphics.Path;

public class StraightLine extends Shape implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7125928496175113220L;

	private PointF start = null;
	private PointF end = null;

	private float slope = 0;
	private float length = 0;

	/**
	 * X coordinates at which to toggle the line on and off, for erasing
	 * purposes.
	 */
	List<Float> lineToggleParameterization = null;

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
		return lineToggleParameterization != null;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> shapes = new ArrayList<Shape>();

		if (lineToggleParameterization.size() > 0) {
			for (int i = 0; i < lineToggleParameterization.size(); i += 2) {
				float startT = lineToggleParameterization.get(i);
				float endT = lineToggleParameterization.get(i+1);

				StraightLine l = new StraightLine(this.mColor, this.mWidth);
				l.addPoint(parameterizationToPoint(startT));
				l.addPoint(parameterizationToPoint(endT));
				shapes.add(l);
			}
		}
		lineToggleParameterization = null;
		invalidatePath();
		return shapes;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (start == null || end == null
				|| !boundingRectangle.intersectsWithCircle(center, radius)) {
			return;
		}

		canonicalizePointOrder();

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

		float dx = x2 - x1;
		float dy = y2 - y1;
		float dsquared = dx * dx + dy * dy;
		float det = x1 * y2 - x2 * y1;
		float discriminant = radius * radius * dsquared - det * det;

		// Intersection if the discriminant is non-negative.  In this
		// case we move on and do even more complicated stuff to erase part of
		// the line.
		if (discriminant > 0) {
			float sign_dy = dy < 0 ? -1 : 1;
			// Now, compute the x coordinates of the real intersection with the
			// line, not the line segment.  Again, this is from Wolfram.
			float intersect1X = (float) ((det * dy - sign_dy * dx * Math.sqrt(discriminant))/dsquared) + center.x;
			float intersect2X = (float) ((det * dy + sign_dy * dx * Math.sqrt(discriminant))/dsquared) + center.x;

			// TODO: Only compute these if the line is sufficiently vertical so
			// as to need the Y coordinate to compute the parameterization.
			float intersect1Y = (float) ((det * dx - Math.abs(dy) * Math.sqrt(discriminant))/dsquared) + center.y;
			float intersect2Y = (float) ((det * dx + Math.abs(dy) * Math.sqrt(discriminant))/dsquared) + center.y;

			float intersect1T = this.pointToParameterization(intersect1X, intersect1Y);
			float intersect2T = this.pointToParameterization(intersect2X, intersect2Y);

			insertErasedSegment(intersect1T, intersect2T);
			invalidatePath();
		}
	}

	private void canonicalizePointOrder() {
		if (end.x < start.x) {
			PointF tmp;
			tmp = start;
			start = end;
			end = tmp;
		}

	}

	/**
	 * @param segmentStart
	 * @param segmentEnd
	 */
	void insertErasedSegment(float segmentStart, float segmentEnd) {
		// Make sure first intersections are ordered
		float tmp;
		if (segmentStart > segmentEnd) {
			tmp = segmentStart;
			segmentStart = segmentEnd;
			segmentEnd = tmp;
		}

		if (lineToggleParameterization == null) {
			 lineToggleParameterization = new ArrayList<Float>();
			 lineToggleParameterization.add(0f);
			 lineToggleParameterization.add(1f);
		}

		// Location in the array before which to insert the first segment
		int segmentStartInsertion = Collections.binarySearch(lineToggleParameterization, segmentStart);
		if (segmentStartInsertion < 0) {
			segmentStartInsertion = -segmentStartInsertion - 1;
		}
		boolean startInDrawnRegion = segmentStartInsertion % 2 == 1;

		// Location in the array before which to insert the last segment.
		int segmentEndInsertion = -Collections.binarySearch(lineToggleParameterization, segmentEnd) - 1;
		if (segmentEndInsertion < 0) {
			segmentEndInsertion = -segmentEndInsertion - 1;
		}
		boolean endInDrawnRegion = segmentEndInsertion % 2 == 1;

		// Remove all segment starts or ends between the insertion points.
		// If we were to run the binary search again, segmentStartInsertion should
		// remain unchanged and segmentEndInsertion should be equal to segmentStartInsertion.
		// Guard this by making sure we don't try to remove from the end of the list.
		if (segmentStartInsertion != lineToggleParameterization.size()) {
			for (int i = 0; i < segmentEndInsertion - segmentStartInsertion; ++i) {
				lineToggleParameterization.remove(segmentStartInsertion);
			}
		}

		if (endInDrawnRegion) {
			lineToggleParameterization.add(segmentStartInsertion, segmentEnd);
		}

		if (startInDrawnRegion) {
			lineToggleParameterization.add(segmentStartInsertion, segmentStart);
		}
	}

	@Override
	protected Path createPath() {
		if (start == null || end == null) {
			return null;
		}
		Path path = new Path();

		if (this.lineToggleParameterization != null) {
			// Erasing has happened, follow erasing instructions.
			boolean on = false;
			for (float toggleT : lineToggleParameterization) {
				PointF togglePoint = parameterizationToPoint(toggleT);
				if (on) {
					path.lineTo(togglePoint.x, togglePoint.y);
				} else {
					path.moveTo(togglePoint.x, togglePoint.y);
				}
				on = !on;
			}
		} else {
			path.moveTo(start.x, start.y);
			path.lineTo(end.x, end.y);
		}

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

	private float pointToParameterization(float x, float y) {
		if (end.y - start.y > end.x - start.x) {
			return (y - start.y) / (end.y - start.y);
		} else {
			return (x - start.x) / (end.x - start.x);
		}
	}

	private PointF parameterizationToPoint(float t) {
		return new PointF(start.x + t * (end.x - start.x),
				          start.y + t * (end.y - start.y));
	}
}
