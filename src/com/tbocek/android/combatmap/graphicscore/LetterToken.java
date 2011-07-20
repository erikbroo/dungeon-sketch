package com.tbocek.android.combatmap.graphicscore;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public final class LetterToken extends BaseToken {
	private String letter;
	
	public LetterToken(String letter) {
		this.letter = letter;
	}
	
	@Override
	public BaseToken clone() {
		return new LetterToken(letter);
	}

	@Override
	public void drawBloodied(Canvas c, float x, float y, float radius) {
		Paint p = new Paint();
		p.setColor(Color.RED);
		draw(c, x, y, radius, p);
	}

	@Override
	public void draw(Canvas c, float x, float y, float radius) {
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		draw(c, x, y, radius, p);
	}
	
	private void draw(Canvas c, float x, float y, float radius, Paint paint) {
		paint.setStrokeWidth(3);
		paint.setStyle(Style.STROKE);
		c.drawCircle(x, y, radius, paint);
		paint.setTextSize(radius);
		paint.setStrokeWidth(2);
		paint.setStyle(Style.FILL);
		c.drawText(letter, x-radius/4, y+radius/4, paint);
	}

	@Override
	public void drawGhost(Canvas c, float x, float y, float radius) {
		//TODO(tim.bocek): Make this look different
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		draw(c, x, y, radius, p);
	}

	@Override
	protected String getTokenClassSpecificId() {
		return letter;
	}
	
	public Set<String> getDefaultTags() {
		Set<String> s = new HashSet<String>();
		s.add("built-in");
		s.add("letter");
		return s;
	}
}
