package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
		Drawable d = getDrawable();
		if (d != null) {
			ColorFilter cf = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.OVERLAY);
			d.setColorFilter(cf);
			draw(c,x,y,radius);
			d.setColorFilter(null);
		}
		
	}

	@Override
	public void draw(Canvas c, float x, float y, float radius) {
		Drawable d = getDrawable();
		if (d != null) {
			c.save(Canvas.CLIP_SAVE_FLAG);
			clipToCircle(c, x, y, radius);
			d.setBounds(new Rect((int)(x-radius), (int)(y-radius),
				                     (int)(x+radius), (int)(y+radius)));
			d.draw(c);
			c.restore();
		}
	}
	
	/**
	 * Sets the clip of the given canvas to a circle centered at (x,y) with radius r
	 * @param c
	 * @param x
	 * @param y
	 * @param radius
	 */
	private void clipToCircle(Canvas c, float x, float y, float radius) {
		Path p = new Path();
		p.addCircle(x, y, radius, Path.Direction.CW);
		c.clipPath(p);
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
