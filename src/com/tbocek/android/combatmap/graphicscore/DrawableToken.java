package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author Tim
 *
 */
public abstract class DrawableToken extends BaseToken {
	private static final long serialVersionUID = -4586968232758191016L;


	@Override
	public void drawBloodied(Canvas c, float x, float y, float radius) {
		// TODO Auto-generated method stub
		draw(c,x,y,radius);
	}

	@Override
	public void draw(Canvas c, float x, float y, float radius) {
		Drawable d = getDrawable();
		if (d != null) {
			d.setBounds(new Rect((int)(x-radius), (int)(y-radius),
				                     (int)(x+radius), (int)(y+radius)));
			d.draw(c);
		}
	}

	@Override
	public void drawGhost(Canvas c, CoordinateTransformer transformer,
			PointF ghostPoint) {
	}
	
	protected Drawable getDrawable() {
		if (mDrawable == null) {
			mDrawable = createDrawable();
		}
		
		return mDrawable;
	}
	
	protected abstract Drawable createDrawable();
	
	private transient Drawable mDrawable;

}
