package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;

public class Token {
	public PointF location = new PointF(0,0);
	// Relative diameter of the token (1.0 = occupies one grid square
	public float size = 1.0f;
	public boolean bloodied = false;
	private int color;
	
	public Token(int c){
		this.color = c;
	}
	
	public void draw(Canvas c, CoordinateTransformer transformer) {
		Paint p = new Paint();
		p.setColor(color);
		PointF center = transformer.worldSpaceToScreenSpace(location);
		
		float radius = transformer.worldSpaceToScreenSpace(this.size * 0.9f / 2);
		c.drawCircle(center.x, center.y, radius, p);
		// Draw bloodied indicator as a thick red border.
		if (bloodied) {
			// If token is already colored red, use a dark red border so it's visible
			p.setColor(color != Color.RED ? Color.RED : Color.rgb(127, 0, 0)); 
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(8);
		}
		c.drawCircle(center.x, center.y, radius-4, p);
	}
	
	/**
	 * Draws an indication of a past location of the token.
	 * @param c
	 * @param transformer
	 * @param ghostPoint Location to draw the ghost, in world space
	 */
	public void drawGhost(Canvas c, CoordinateTransformer transformer, PointF ghostPoint) {
		Paint p = new Paint();
		p.setStrokeWidth(2);
		p.setColor(color);
		p.setStyle(Style.STROKE);
		PointF center = transformer.worldSpaceToScreenSpace(ghostPoint);
		
		float radius = transformer.worldSpaceToScreenSpace(this.size * 0.9f / 2);
		
		c.drawCircle(center.x, center.y, radius, p);		
	}
	
	public void drawPreview(Canvas c, float x, float y, float radius) {
		Paint p = new Paint();
		p.setColor(color);
		c.drawCircle(x, y, radius, p);
	}

	public void move(float distanceX, float distanceY) {
		location = new PointF(location.x - distanceX, location.y - distanceY);
		
	}
	
	public Token clone() {
		return new Token(color);
	}

	public void setDiameter(float d) {
		this.size = d;
		
	}
}
