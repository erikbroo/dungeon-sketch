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

	public String mText;

	public float mTextSize;

	public PointF mLocation;
	
	private boolean mErased;
	
	public static boolean drawBoundingBoxes;

	public Text(
			String newText, float size, int color, float strokeWidth, 
			PointF location, CoordinateTransformer transform) {
		this.mText = newText;
		this.mTextSize = size;
		this.mColor = color;
		this.mWidth = strokeWidth;
		
		this.mLocation = location;
		
		// Compute the bounding rectangle.
		// To do this, we need to create the Paint object so we know the size
		// of the text.
		ensurePaintCreated();
		this.mPaint.setTextSize(mTextSize);
		
		Rect bounds = new Rect();
		mPaint.getTextBounds(mText, 0, mText.length(), bounds);
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
		mText = copyFrom.mText;
		mTextSize = copyFrom.mTextSize;
		this.mColor = copyFrom.mColor;
		this.mWidth = copyFrom.mWidth;
		this.mBoundingRectangle = new BoundingRectangle();
		this.mBoundingRectangle.updateBounds(copyFrom.mBoundingRectangle);
		this.mLocation = new PointF(copyFrom.mLocation.x, copyFrom.mLocation.y);
	}

	@Override
	public boolean contains(PointF p) {
		return this.mBoundingRectangle.contains(p);
	}

	@Override
	public boolean needsOptimization() {
		// TODO Auto-generated method stub
		return mErased;
	}

	@Override
	public List<Shape> removeErasedPoints() {
		List<Shape> ret = new ArrayList<Shape>();
		if (!mErased) {
			ret.add(this);
		} else {
			mErased = false;
		}
		return ret;
	}

	@Override
	public void erase(PointF center, float radius) {
		if (this.mBoundingRectangle.intersectsWithCircle(center, radius)) {
			mErased = true;
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
		this.mPaint.setTextSize(mTextSize);
		if (Text.drawBoundingBoxes) {
			this.mPaint.setStyle(Style.STROKE);
			c.drawRect(this.mBoundingRectangle.toRectF(), mPaint);
			this.mPaint.setStyle(Style.FILL);
		}
		c.drawText(mText, mLocation.x, mLocation.y, this.mPaint);
	}
	
	protected Shape getMovedShape(float deltaX, float deltaY) {
		Text t = new Text(this);
		
		t.mLocation.x += deltaX;
		t.mLocation.y += deltaY;
		t.mBoundingRectangle.move(deltaX, deltaY);
		
		return t;
	}
	
	public boolean shouldDrawBelowGrid() {
		return false;
	}
	
    public void serialize(MapDataSerializer s) throws IOException {
    	serializeBase(s, SHAPE_TYPE);
    	
    	s.startObject();
    	s.serializeString(this.mText);
    	s.serializeFloat(this.mTextSize);
    	s.serializeFloat(this.mLocation.x);
    	s.serializeFloat(this.mLocation.y);
    	s.endObject();
    }

	@Override
	protected void shapeSpecificDeserialize(MapDataDeserializer s) throws IOException {
		s.expectObjectStart();
		this.mText = s.readString();
		this.mTextSize = s.readFloat();
		mLocation = new PointF();
		this.mLocation.x = s.readFloat();
		this.mLocation.y = s.readFloat();
		s.expectObjectEnd();
	}

}
