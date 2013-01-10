package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Path;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

/**
 * Represents a drawn rectangle.
 * 
 * @author Tim
 * 
 */
public class Rectangle extends Shape {
	/**
	 * Short character string that is the type of the shape.
	 */
	public static final String SHAPE_TYPE = "rct";

	/**
	 * Upper right corner of the rectangle.
	 */
	private PointF mP2;

	/**
	 * Lower left corner of the rectangle.
	 */
	private PointF mP1;

	/**
	 * Line to use when erasing portions of the rectangle.
	 */
	private FreehandLine mLineForErasing;

	/**
	 * Constructor.
	 * 
	 * @param color
	 *            Line color.
	 * @param width
	 *            Stroke width.
	 */
	public Rectangle(int color, float width) {
		this.setColor(color);
		this.setWidth(width);
	}

	@Override
	public boolean contains(PointF p) {
		return this.getBoundingRectangle().contains(p);
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
		if (this.getBoundingRectangle().intersectsWithCircle(center, radius)) {
			if (mLineForErasing == null) {
				float xmin = Math.min(mP1.x, mP2.x);
				float ymin = Math.min(mP1.y, mP2.y);
				float xmax = Math.max(mP1.x, mP2.x);
				float ymax = Math.max(mP1.y, mP2.y);
				mLineForErasing = new FreehandLine(getColor(), getStrokeWidth());
				mLineForErasing.addPoint(new PointF(xmin, ymin));
				mLineForErasing.addPoint(new PointF(xmin, ymax));
				mLineForErasing.addPoint(new PointF(xmax, ymax));
				mLineForErasing.addPoint(new PointF(xmax, ymin));
				mLineForErasing.addPoint(new PointF(xmin, ymin));
			}
			mLineForErasing.erase(center, radius);
			invalidatePath();
		}

	}

	@Override
	protected Path createPath() {
		if (!isValid()) {
			return null;
		}
		if (mLineForErasing != null) {
			return mLineForErasing.createPath();
		} else {
			Path p = new Path();

			p.addRect(Math.min(mP1.x, mP2.x), Math.min(mP1.y, mP2.y),
					Math.max(mP1.x, mP2.x), Math.max(mP1.y, mP2.y),
					Path.Direction.CW);
			return p;
		}
	}

	@Override
	public void addPoint(PointF p) {
		if (mP1 == null) {
			mP1 = p;
		} else {
			mP2 = p;
			// Re-create the bounding rectangle every time this is done.
			getBoundingRectangle().clear();
			getBoundingRectangle().updateBounds(mP1);
			getBoundingRectangle().updateBounds(mP2);
			invalidatePath();
		}

	}

	@Override
	public void serialize(MapDataSerializer s) throws IOException {
		serializeBase(s, SHAPE_TYPE);

		s.startObject();
		s.serializeFloat(mP1.x);
		s.serializeFloat(mP1.y);
		s.serializeFloat(mP2.x);
		s.serializeFloat(mP2.y);
		s.endObject();

	}

	@Override
	public boolean isValid() {
		return mP2 != null && mP1 != null;
	}

	@Override
	protected void shapeSpecificDeserialize(MapDataDeserializer s)
			throws IOException {
		s.expectObjectStart();
		mP1 = new PointF();
		mP1.x = s.readFloat();
		mP1.y = s.readFloat();
		mP2 = new PointF();
		mP2.x = s.readFloat();
		mP2.y = s.readFloat();
		s.expectObjectEnd();
	}

}
