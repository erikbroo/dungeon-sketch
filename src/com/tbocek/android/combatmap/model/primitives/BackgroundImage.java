package com.tbocek.android.combatmap.model.primitives;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class BackgroundImage {
	private PointF mOriginWorldSpace;
	private float mWidthWorldSpace = 1;
	private float mHeightWorldSpace = 1;

	private Drawable mDrawable;
	
	public BackgroundImage(Drawable drawable) {
		mDrawable = drawable;
	}
	
	public void setLocation(PointF location) {
		mOriginWorldSpace = location;
	}
	
	/**
	 * Draws the background image.  Needs to assume an untransformed coordinate
	 * space, UNLIKE other draw commands.  The coordinate transformer is passed
	 * in instead.  This is because setBounds is retarded and takes integers.
	 * @param c Canvas to draw on.
	 * @param transformer The screen to world space transformer.
	 */
	public void draw(Canvas c, CoordinateTransformer transformer) {
		// Convert bounding rectangle bounds to screen space.
		PointF upperLeft = transformer.worldSpaceToScreenSpace(new PointF(mOriginWorldSpace.x, mOriginWorldSpace.y));
		PointF lowerRight = transformer.worldSpaceToScreenSpace(new PointF(mOriginWorldSpace.x + mWidthWorldSpace, mOriginWorldSpace.y + mHeightWorldSpace));
		int left = (int) upperLeft.x;
		int right = (int) lowerRight.x;
		int top = (int) upperLeft.y;
		int bottom = (int) lowerRight.y;
		
		mDrawable.setBounds(left, top, right, bottom);
		
		mDrawable.draw(c);
	}

	public BoundingRectangle getBoundingRectangle(float borderWorldSpace) {
		PointF p1 = new PointF(mOriginWorldSpace.x - borderWorldSpace, mOriginWorldSpace.y - borderWorldSpace);
		PointF p2 = new PointF(mOriginWorldSpace.x + mWidthWorldSpace  + borderWorldSpace, mOriginWorldSpace.y + mHeightWorldSpace + borderWorldSpace);
		return new BoundingRectangle(p1, p2);
	}
	
	public BoundingRectangle getBoundingRectangle() {
		return getBoundingRectangle(0);
	}
	
	public void moveTop(float delta) {
		this.mOriginWorldSpace.y += delta;
		this.mHeightWorldSpace -= delta;
	}
	
	public void moveBottom(float delta) {
		this.mHeightWorldSpace += delta;
	}
	
	public void moveLeft(float delta) {
		this.mOriginWorldSpace.x += delta;
		this.mWidthWorldSpace -= delta;
	}
	
	public void moveRight(float delta) {
		this.mWidthWorldSpace += delta;
	}
	
	public void moveImage(float deltaX, float deltaY) {
		this.mOriginWorldSpace = new PointF(this.mOriginWorldSpace.x + deltaX,
		                                    this.mOriginWorldSpace.y + deltaY);
	}
}
