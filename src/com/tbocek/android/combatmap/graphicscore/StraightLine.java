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

	/**
	 * X coordinates at which to toggle the line on and off, for erasing
	 * purposes.
	 */
	List<Float> lineToggleXCoords = null;

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
		return lineToggleXCoords != null;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> shapes = new ArrayList<Shape>();

		if (start.x < end.x) {
			for (int i = 0; i <= lineToggleXCoords.size(); i += 2) {
				float startX = (i == 0) ? start.x : lineToggleXCoords.get(i-1);
				float endX = (i == lineToggleXCoords.size()) ? end.x : lineToggleXCoords.get(i);

				float startY = ycoord(startX);
				float endY = ycoord(endX);

				StraightLine l = new StraightLine(this.mColor, this.mWidth);
				l.addPoint(new PointF(startX, startY));
				l.addPoint(new PointF(endX, endY));
				shapes.add(l);
			}
		}
		lineToggleXCoords = null;
		invalidatePath();
		return shapes;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (start == null || end == null
				|| !boundingRectangle.intersectsWithCircle(center, radius)) {
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

		float dx = x2 - x1;
		float dy = y2 - y1;
		float dsquared = dx * dx + dy * dy;
		float det = x1 * y2 - x2 * y1;
		float discriminant = radius * radius * dsquared - det * det;

		// Intersection if the discriminant is non-negative.  In this
		// case we move on and do even more complicated stuff to erase part of
		// the line.
		if (discriminant > 0) {
			// First, we need to make sure that the x coordinate of the start
			// comes before the end, since this assumption will be used later.
			canonicalizePointOrder();

			// Now, compute the x coordinates of the real intersection with the
			// line, not the line segment.  Again, this is from Wolfram.
			float intersect1X = (float) ((det * dy - dx * Math.sqrt(discriminant))/dsquared) + center.x;
			float intersect2X = (float) ((det * dy + dx * Math.sqrt(discriminant))/dsquared) + center.x;

			insertErasedSegment(intersect1X, intersect2X);
			invalidatePath();
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

		if (lineToggleXCoords == null) {
			 lineToggleXCoords = new ArrayList<Float>();
		}

		// Location in the array before which to insert the first segment
		int segmentStartInsertion = Collections.binarySearch(lineToggleXCoords, segmentStart);
		if (segmentStartInsertion < 0) {
			segmentStartInsertion = -segmentStartInsertion - 1;
		}
		boolean startInDrawnRegion = segmentStartInsertion % 2 == 0;

		// Location in the array before which to insert the last segment.
		int segmentEndInsertion = -Collections.binarySearch(lineToggleXCoords, segmentEnd) - 1;
		if (segmentEndInsertion < 0) {
			segmentEndInsertion = -segmentEndInsertion - 1;
		}
		boolean endInDrawnRegion = segmentEndInsertion % 2 == 0;

		// Remove all segment starts or ends between the insertion points.
		// If we were to run the binary search again, segmentStartInsertion should
		// remain unchanged and segmentEndInsertion should be equal to segmentStartInsertion.
		// Guard this by making sure we don't try to remove from the end of the list.
		if (segmentStartInsertion != lineToggleXCoords.size()) {
			for (int i = 0; i < segmentEndInsertion - segmentStartInsertion; ++i) {
				lineToggleXCoords.remove(segmentStartInsertion);
			}
		}

		if (endInDrawnRegion) {
			lineToggleXCoords.add(segmentStartInsertion, segmentEnd);
		}

		if (startInDrawnRegion) {
			lineToggleXCoords.add(segmentStartInsertion, segmentStart);
		}

		// Special cases for erasing the ends of the line segment.
		PointF newStart = this.start;
		PointF newEnd = this.end;

		if (segmentStart < start.x && segmentEnd > start.x) {
			newStart = new PointF(segmentEnd, ycoord(segmentEnd));
		}

		if (segmentEnd > end.x && segmentStart < end.x) {
			newEnd = new PointF(segmentStart, ycoord(segmentStart));
		}
		this.start = newStart;
		this.end = newEnd;

		this.removeOutOfBoundErasePoints();
	}

	private void removeOutOfBoundErasePoints() {
		if (lineToggleXCoords != null) {
			ArrayList<Float> newToggles = new ArrayList<Float>();
			for (float f: lineToggleXCoords) {
				if (f > start.x && f < end.x) {
					newToggles.add(f);
				}
			}
			lineToggleXCoords = newToggles;
		}
	}

	private void canonicalizePointOrder() {
		PointF temp;
		if (start.x > end.x) {
			temp = start;
			start = end;
			end = temp;
		}
	}

	@Override
	protected Path createPath() {
		if (start == null || end == null || start.x > end.x) {
			return null;
		}
		Path path = new Path();


		// Erasing has happened, follow erasing instructions.
		path.moveTo(start.x, start.y);
		boolean on = true;

		if (this.lineToggleXCoords != null) {
			for (float toggleX : lineToggleXCoords) {
				float toggleY = ycoord(toggleX);
				if (on) {
					path.lineTo(toggleX, toggleY);
				} else {
					path.moveTo(toggleX, toggleY);
				}
				on = !on;
			}
		}

		if (on) {
			path.lineTo(end.x, end.y);
		}

		return path;
	}

	/**
	 * Get the y coordinate for the given x coordinate for this line.
	 * @param x
	 * @return
	 */
	private float ycoord(float x) {
		return slope * (x - start.x) + start.y;
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
			slope = (end.y - start.y) / (end.x - start.x);
			invalidatePath();
		}
	}
}
