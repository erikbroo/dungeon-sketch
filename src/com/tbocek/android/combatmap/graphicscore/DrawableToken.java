package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author Tim
 *
 */
public abstract class DrawableToken extends BaseToken {
	private static final long serialVersionUID = -4586968232758191016L;

	@Override
	public BaseToken clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawBloodied(Canvas c, float x, float y, float radius) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Canvas c, float x, float y, float radius) {
		// TODO Auto-generated method stub

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
