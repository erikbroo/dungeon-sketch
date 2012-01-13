package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

import android.graphics.Path;

/**
 * Shape class that represents a single straight line segment.  Contains
 * methods for manipulating portions of the line segment based on a
 * parameterization of the segment.
 * @author Tim
 *
 */
public class StraightLine extends Shape {

	/**
	 * Short character string that is the type of the shape.
	 */
	public static final String SHAPE_TYPE = "sl";

	/**
	 * First endpoint on the line.  X coordinate guaranteed to be less than x
	 * coordinate of mEnd.
	 */
	private PointF mStart;
	
	/**
	 * Second endpoint on the line.  X coordinate guaranteed to be greater than
	 * x coordinate of mStart.
	 */
	private PointF mEnd;

	/**
	 * Where to toggle the line on and off, for erasing purposes.  These
	 * values are parameterized by the length of the line, so that all
	 * values fall in the range [0,1].  
	 */
	private List<Float> mLineToggleParameterization;

	/**
	 * Constructor.
	 * @param color Color of the line.
	 * @param newLineStrokeWidth Stroke width of the line.
	 */
	public StraightLine(int color, float newLineStrokeWidth) {
        this.setColor(color);
        this.setWidth(newLineStrokeWidth);
	}

	@Override
	public boolean contains(PointF p) {
		// Cannot define a region.
		return false;
	}

	@Override
	public boolean needsOptimization() {
		return mLineToggleParameterization != null;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> shapes = new ArrayList<Shape>();

		if (mLineToggleParameterization.size() > 0) {
			for (int i = 0; i < mLineToggleParameterization.size(); i += 2) {
				float startT = mLineToggleParameterization.get(i);
				float endT = mLineToggleParameterization.get(i + 1);

				StraightLine l = new StraightLine(this.getColor(), this.getWidth());
				l.addPoint(parameterizationToPoint(startT));
				l.addPoint(parameterizationToPoint(endT));
				shapes.add(l);
			}
		}
		mLineToggleParameterization = null;
		invalidatePath();
		return shapes;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (mStart == null || mEnd == null
				|| !getBoundingRectangle().intersectsWithCircle(
						center, radius)) {
			return;
		}

		canonicalizePointOrder();

		// Special case - if we have only two points, this is probably
		// a large straight line and we want to erase the line if the
		// eraser intersects with it.  However, this is an expensive
		// test, so we don't want to do it for all line segments when
		// they are generally small enough for the eraser to enclose.
		Util.IntersectionPair intersection = Util.lineCircleIntersection(mStart, mEnd, center, radius);

		if (intersection != null) {
			float intersect1T = this.pointToParameterization(intersection.getIntersection1());
			float intersect2T = this.pointToParameterization(intersection.getIntersection2());

			insertErasedSegment(intersect1T, intersect2T);
			invalidatePath();
		}
	}

	/**
	 * Makes sure that start point's X coordinate occurs before the end point's
	 * X coordinate.  This assumption is used elsewhere.
	 */
	private void canonicalizePointOrder() {
		if (mEnd.x < mStart.x) {
			PointF tmp;
			tmp = mStart;
			mStart = mEnd;
			mEnd = tmp;
		}
	}

	/**
	 * 
	 * @param segmentStart Start of the erased segment, parameterized by the
	 * 		length of the line.
	 * @param segmentEnd End of the erased segment, parameterized by the length
	 * 		of the line.
	 */
	void insertErasedSegment(float segmentStart, float segmentEnd) {
		// Make sure first intersections are ordered
		float tmp;
		if (segmentStart > segmentEnd) {
			tmp = segmentStart;
			segmentStart = segmentEnd;
			segmentEnd = tmp;
		}

		if (mLineToggleParameterization == null) {
			 mLineToggleParameterization = new ArrayList<Float>();
			 mLineToggleParameterization.add(0f);
			 mLineToggleParameterization.add(1f);
		}

		// Location in the array before which to insert the first segment
		int segmentStartInsertion = Collections.binarySearch(mLineToggleParameterization, segmentStart);
		if (segmentStartInsertion < 0) {
			segmentStartInsertion = -segmentStartInsertion - 1;
		}
		boolean startInDrawnRegion = segmentStartInsertion % 2 == 1;

		// Location in the array before which to insert the last segment.
		int segmentEndInsertion = -Collections.binarySearch(mLineToggleParameterization, segmentEnd) - 1;
		if (segmentEndInsertion < 0) {
			segmentEndInsertion = -segmentEndInsertion - 1;
		}
		boolean endInDrawnRegion = segmentEndInsertion % 2 == 1;

		// Remove all segment starts or ends between the insertion points.
		// If we were to run the binary search again, segmentStartInsertion should
		// remain unchanged and segmentEndInsertion should be equal to segmentStartInsertion.
		// Guard this by making sure we don't try to remove from the end of the list.
		if (segmentStartInsertion != mLineToggleParameterization.size()) {
			for (int i = 0; i < segmentEndInsertion - segmentStartInsertion; ++i) {
				mLineToggleParameterization.remove(segmentStartInsertion);
			}
		}

		if (endInDrawnRegion) {
			mLineToggleParameterization.add(segmentStartInsertion, segmentEnd);
		}

		if (startInDrawnRegion) {
			mLineToggleParameterization.add(segmentStartInsertion, segmentStart);
		}
	}

	@Override
	public Path createPath() {
		if (mStart == null || mEnd == null) {
			return null;
		}
		Path path = new Path();

		if (this.mLineToggleParameterization != null) {
			// Erasing has happened, follow erasing instructions.
			boolean on = false;
			for (float toggleT : mLineToggleParameterization) {
				PointF togglePoint = parameterizationToPoint(toggleT);
				if (on) {
					path.lineTo(togglePoint.x, togglePoint.y);
				} else {
					path.moveTo(togglePoint.x, togglePoint.y);
				}
				on = !on;
			}
		} else {
			path.moveTo(mStart.x, mStart.y);
			path.lineTo(mEnd.x, mEnd.y);
		}

		return path;
	}

	@Override
	public void addPoint(PointF p) {
		if (mStart == null) {
			mStart = p;
		} else {
			mEnd = p;
			// Re-create the bounding rectangle every time this is done.
			getBoundingRectangle().clear();
			getBoundingRectangle().updateBounds(mStart);
			getBoundingRectangle().updateBounds(mEnd);
			invalidatePath();
		}
	}

	/**
	 * Given a point, gives a distance scaled to the length of the line where
	 * that point falls on the line.
	 * @param p The point to parameterize.  Must fall on the line.
	 * @return Distance along the line segment where the point falls, scaled
	 * 		to the range [0,1].
	 */
	private float pointToParameterization(PointF p) {
		if (Math.abs(mEnd.y - mStart.y) > Math.abs(mEnd.x - mStart.x)) {
			return (p.y - mStart.y) / (mEnd.y - mStart.y);
		} else {
			return (p.x - mStart.x) / (mEnd.x - mStart.x);
		}
	}

	/**
	 * Given a float in the range [0,1] that represents a distance along this
	 * line scaled to the length of the line, returns a the coordinates where
	 * that distance occurs on the line.
	 * @param t The parameterized distance.
	 * @return The point where that parameterization occurs on the line.
	 */
	private PointF parameterizationToPoint(float t) {
		return new PointF(mStart.x + t * (mEnd.x - mStart.x),
				          mStart.y + t * (mEnd.y - mStart.y));
	}
	
	@Override
    public void serialize(MapDataSerializer s) throws IOException {
    	serializeBase(s, SHAPE_TYPE);
    	
    	s.startObject();
    	s.serializeFloat(mStart.x);
    	s.serializeFloat(mStart.y);
    	s.serializeFloat(mEnd.x);
    	s.serializeFloat(mEnd.y);
    	s.endObject();
    }

	@Override
	protected void shapeSpecificDeserialize(MapDataDeserializer s)
			throws IOException {
		s.expectObjectStart();
		mStart = new PointF();
		mStart.x = s.readFloat();
		mStart.y = s.readFloat();
		mEnd = new PointF();
		mEnd.x = s.readFloat();
		mEnd.y = s.readFloat();
		s.expectObjectEnd();
	}
}
