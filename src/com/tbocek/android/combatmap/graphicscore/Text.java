package com.tbocek.android.combatmap.graphicscore;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

public class Text extends Shape {

	private String mText;

	private float mSize;

	private PointF location;
	
	private boolean erased = false;

	public Text(String text, float size, int color, float strokeWidth, PointF location, CoordinateTransformer transform) {
		this.mText = text;
		this.mSize = size;
		this.mColor = color;
		this.mWidth = strokeWidth;
		
		this.location = location;
		
		// Compute the bounding rectangle.
		// To do this, we need to create the Paint object so we know the size
		// of the text.
		ensurePaintCreated();
		this.paint.setTextSize(mSize);
		
		Rect bounds = new Rect();
		paint.getTextBounds(mText, 0, mText.length(), bounds);
		this.boundingRectangle.updateBounds(location);
		this.boundingRectangle.updateBounds(
				new PointF(
						location.x + bounds.width(), 
						location.y - bounds.height()));
	}

	@Override
	public boolean contains(PointF p) {
		// TODO Auto-generated method stub
		return false;
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
		if (this.boundingRectangle.intersectsWithCircle(center, radius)) {
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
		this.paint.setTextSize(mSize);
		this.paint.setStyle(Style.STROKE);
		c.drawRect(this.boundingRectangle.toRectF(), paint);
		this.paint.setStyle(Style.FILL);
		c.drawText(mText, location.x, location.y, this.paint);
	}

}
