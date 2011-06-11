package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.PointF;

public abstract class BaseToken {

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

	public abstract void drawBloodied(Canvas c, float x, float y,
			float radius);

	public abstract void draw(Canvas c, float x, float y,
			float radius);

	public abstract void drawGhost(Canvas c, CoordinateTransformer transformer, PointF ghostPoint);

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

}