package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public abstract class BaseToken implements Serializable{
	private static final long serialVersionUID = 9080531944602251588L;
	
	private PointF location = new PointF(0,0);
	private float size = 1.0f;
	private boolean bloodied = false;

	public BaseToken() {
		super();
	}

	public void move(float distanceX, float distanceY) {
		setLocation(new PointF(getLocation().x - distanceX, getLocation().y - distanceY));
		
	}

	public void setDiameter(float d) {
		this.setSize(d);
	}

	public abstract BaseToken clone();

	/**
	 * Draw a bloodied version of the token at the given coordinates and size.  Everything
	 * in screen space.
	 * @param c
	 * @param x
	 * @param y
	 * @param radius
	 */
	public abstract void drawBloodied(Canvas c, float x, float y,
			float radius);

	/**
	 * Draw the token at the given coordinates and size.  Everything in screen space.
	 * @param c
	 * @param x
	 * @param y
	 * @param radius
	 */
	public abstract void draw(Canvas c, float x, float y,
			float radius);

	protected abstract void drawGhost(Canvas c, float x, float y, float radius);
	
	public final void drawGhost(Canvas c, CoordinateTransformer transformer, PointF ghostPoint) {
		PointF center = transformer.worldSpaceToScreenSpace(ghostPoint);
		float radius = transformer.worldSpaceToScreenSpace(this.getSize() * 0.9f / 2);	
		drawGhost(c, center.x, center.y, radius);
	}

	public void drawInPosition(Canvas c, CoordinateTransformer transformer) {
		PointF center = transformer.worldSpaceToScreenSpace(getLocation());
		float radius = transformer.worldSpaceToScreenSpace(this.getSize() * 0.9f / 2);
	
		if (isBloodied()) {
			drawBloodied(c, center.x, center.y, radius);
		} else {
			draw(c, center.x, center.y, radius);
		}
	}

	public void setBloodied(boolean bloodied) {
		this.bloodied = bloodied;
	}

	public boolean isBloodied() {
		return bloodied;
	}

	public void setLocation(PointF location) {
		this.location = location;
	}

	public PointF getLocation() {
		return location;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public float getSize() {
		return size;
	}
	
	public BoundingRectangle getBoundingRectangle() {
		BoundingRectangle r = new BoundingRectangle();
		r.updateBounds(new PointF(location.x - size/2, location.y - size/2));
		r.updateBounds(new PointF(location.x + size/2, location.y + size/2));
		return r;
	}


}