package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.PointF;

public abstract class BaseToken {

	public PointF location = new PointF(0,0);
	public float size = 1.0f;
	public boolean bloodied = false;

	public BaseToken() {
		super();
	}

	public void move(float distanceX, float distanceY) {
		location = new PointF(location.x - distanceX, location.y - distanceY);
		
	}

	public void setDiameter(float d) {
		this.size = d;
	}

	public abstract BaseToken clone();

	public abstract void drawBloodied(Canvas c, float x, float y,
			float radius);

	public abstract void draw(Canvas c, float x, float y,
			float radius);

	public abstract void drawGhost(Canvas c, CoordinateTransformer transformer, PointF ghostPoint);

	public void drawInPosition(Canvas c, CoordinateTransformer transformer) {
		PointF center = transformer.worldSpaceToScreenSpace(location);
		float radius = transformer.worldSpaceToScreenSpace(this.size * 0.9f / 2);
	
		if (bloodied) {
			drawBloodied(c, center.x, center.y, radius);
		} else {
			draw(c, center.x, center.y, radius);
		}
	}

}