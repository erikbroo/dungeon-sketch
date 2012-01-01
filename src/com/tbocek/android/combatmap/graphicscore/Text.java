package com.tbocek.android.combatmap.graphicscore;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

public class Text extends Shape {

	public String text;

	public float textSize;

	PointF location;
	
	private boolean erased = false;
	
	public static boolean drawBoundingBoxes = false;

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
		this.paint.setTextSize(textSize);
		
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		this.boundingRectangle.updateBounds(location);
		this.boundingRectangle.updateBounds(
				new PointF(
						location.x + bounds.width(), 
						location.y - bounds.height()));
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
		this.boundingRectangle = new BoundingRectangle();
		this.boundingRectangle.updateBounds(copyFrom.boundingRectangle);
		this.location = new PointF(copyFrom.location.x, copyFrom.location.y);
	}

	@Override
	public boolean contains(PointF p) {
		return this.boundingRectangle.contains(p);
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
		this.paint.setTextSize(textSize);
		if (Text.drawBoundingBoxes) {
			this.paint.setStyle(Style.STROKE);
			c.drawRect(this.boundingRectangle.toRectF(), paint);
			this.paint.setStyle(Style.FILL);
		}
		c.drawText(text, location.x, location.y, this.paint);
	}
	
	protected Shape getMovedShape(float deltaX, float deltaY) {
		Text t = new Text(this);
		
		t.location.x += deltaX;
		t.location.y += deltaY;
		t.boundingRectangle.move(deltaX, deltaY);
		
		return t;
	}
	
	public boolean shouldDrawBelowGrid() {
		return false;
	}

}
