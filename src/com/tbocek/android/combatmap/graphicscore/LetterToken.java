package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;

public class LetterToken extends BaseToken {
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
	public void drawGhost(Canvas c, CoordinateTransformer transformer,
			PointF ghostPoint) {
		// TODO Auto-generated method stub
	}

}
