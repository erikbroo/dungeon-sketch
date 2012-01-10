package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;

import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

public class Text extends Shape {

	/**
	 * Short character string that is the type of the shape.
	 */
	public static final String SHAPE_TYPE = "txt";

	public String text;

	public float textSize;

	public PointF location;
	
	private boolean erased;
	
	public static boolean drawBoundingBoxes;

	public Text(String newText, float size, int color, float strokeWidth, PointF location, CoordinateTransformer transform) {
		this.text = newText;
		this.textSize = size;
		this.mColor = color;
		this.mWidth = strokeWidth;
		
		this.location = location;
		
		// Compute the bounding rectangle.
		// To do this, we need to create the Paint object so we know the size
		// of the text.
		ensurePaintCreated();
		this.mPaint.setTextSize(textSize);
		
		Rect bounds = new Rect();
		mPaint.getTextBounds(text, 0, text.length(), bounds);
		this.mBoundingRectangle.updateBounds(location);
		this.mBoundingRectangle.updateBounds(
				new PointF(
						location.x + bounds.width(), 
						location.y - bounds.height()));
	}
	
	/**
	 * HACK: Ctor for deserialization ONLY!!!  The bounding rectangle in
	 * particular MUST be manually set!!!
	 * @param color
	 * @param strokeWidth
	 */
	Text(int color, float strokeWidth) {
		this.mColor = color;
		this.mWidth = strokeWidth;
	}
	
	/**
	 * Copy constructor.
	 * @param copyFrom
	 */
	public Text(Text copyFrom) {
		text = copyFrom.text;
		textSize = copyFrom.textSize;
		this.mColor = copyFrom.mColor;
		this.mWidth = copyFrom.mWidth;
		this.mBoundingRectangle = new BoundingRectangle();
		this.mBoundingRectangle.updateBounds(copyFrom.mBoundingRectangle);
		this.location = new PointF(copyFrom.location.x, copyFrom.location.y);
	}

	@Override
	public boolean contains(PointF p) {
		return this.mBoundingRectangle.contains(p);
	}

	@Override
	public boolean needsOptimization() {
		// TODO Auto-generated method stub
		return erased;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> ret = new ArrayList<Shape>();
		if (!erased) {
			ret.add(this);
		} else {
			erased = false;
		}
		return ret;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (this.mBoundingRectangle.intersectsWithCircle(center, radius)) {
			erased = true;
		}
	}

	@Override
	protected Path createPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPoint(PointF p) {
		throw new RuntimeException("Adding point to text not supported.");
	}

	@Override
	public void draw(final Canvas c) {
		ensurePaintCreated();
		this.mPaint.setTextSize(textSize);
		if (Text.drawBoundingBoxes) {
			this.mPaint.setStyle(Style.STROKE);
			c.drawRect(this.mBoundingRectangle.toRectF(), mPaint);
			this.mPaint.setStyle(Style.FILL);
		}
		c.drawText(text, location.x, location.y, this.mPaint);
	}
	
	protected Shape getMovedShape(float deltaX, float deltaY) {
		Text t = new Text(this);
		
		t.location.x += deltaX;
		t.location.y += deltaY;
		t.mBoundingRectangle.move(deltaX, deltaY);
		
		return t;
	}
	
	public boolean shouldDrawBelowGrid() {
		return false;
	}
	
    public void serialize(MapDataSerializer s) throws IOException {
    	serializeBase(s, SHAPE_TYPE);
    	
    	s.startObject();
    	s.serializeString(this.text);
    	s.serializeFloat(this.textSize);
    	s.serializeFloat(this.location.x);
    	s.serializeFloat(this.location.y);
    	s.endObject();
    }

	@Override
	protected void shapeSpecificDeserialize(MapDataDeserializer s) throws IOException {
		s.expectObjectStart();
		this.text = s.readString();
		this.textSize = s.readFloat();
		location = new PointF();
		this.location.x = s.readFloat();
		this.location.y = s.readFloat();
		s.expectObjectEnd();
	}

}
