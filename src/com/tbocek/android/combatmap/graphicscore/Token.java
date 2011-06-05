package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

public class Token {
	public PointF location = new PointF(0,0);
	public float radius = 30;
	public boolean bloodied = false;
	private int color;
	
	public Token(int c){
		this.color = c;
	}
	
	public void draw(Canvas c, CoordinateTransformer transformer) {
		Paint p = new Paint();
		p.setColor(bloodied ? Color.RED : color);
		PointF center = transformer.worldSpaceToScreenSpace(location);
		float radius = transformer.worldSpaceToScreenSpace(this.radius);
		
		c.drawCircle(center.x, center.y, radius, p);
	}

	public void move(float distanceX, float distanceY) {
		location = new PointF(location.x - distanceX, location.y - distanceY);
		
	}
	
	public Token clone() {
		return new Token(color);
	}
}
