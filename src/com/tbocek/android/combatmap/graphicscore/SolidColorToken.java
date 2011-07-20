package com.tbocek.android.combatmap.graphicscore;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public final class SolidColorToken extends BaseToken {
	private int color;
	
	public SolidColorToken(int c){
		this.color = c;
	}
	
	/**
	 * Draws an indication of a past location of the token.
	 * @param c
	 * @param transformer
	 * @param ghostPoint Location to draw the ghost, in world space
	 */
	@Override
	public void drawGhost(Canvas c, float x, float y, float radius) {
		Paint p = new Paint();
		p.setStrokeWidth(2);
		p.setColor(color);
		p.setStyle(Style.STROKE);
		c.drawCircle(x, y, radius, p);		
	}
	
	@Override
	public void draw(Canvas c, float x, float y, float radius) {
		Paint p = new Paint();
		p.setColor(color);
		c.drawCircle(x, y, radius, p);
	}
	
	@Override
	public void drawBloodied(Canvas c, float x, float y, float radius) {
		draw(c, x, y, radius);
		
		Paint p = new Paint();
		// If token is already colored red, use a dark red border so it's visible
		p.setColor(color != Color.RED ? Color.RED : Color.rgb(127, 0, 0)); 
		p.setStyle(Style.STROKE);
		p.setStrokeWidth(8);
		c.drawCircle(x, y, radius-4, p);
	}
	
	@Override
	public BaseToken clone() {
		return new SolidColorToken(color);
	}

	@Override
	protected String getTokenClassSpecificId() {
		return Integer.toString(color);
	}
	
	public Set<String> getDefaultTags() {
		Set<String> s = new HashSet<String>();
		s.add("built-in");
		s.add("solid color");
		return s;
	}
}
