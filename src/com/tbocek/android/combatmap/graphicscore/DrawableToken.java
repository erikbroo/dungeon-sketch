package com.tbocek.android.combatmap.graphicscore;

import java.util.HashMap;
import java.util.Map;

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

    private static final int FULL_OPACITY = 255;
    private static final int HALF_OPACITY = 128;

    private static Map<String, Drawable> drawableCache = new HashMap<String, Drawable>();

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
    protected void drawGhost(Canvas c, float x, float y, float radius) {
        Drawable d = getDrawable();
        if (d != null) {
            d.setAlpha(HALF_OPACITY);
            draw(c, x, y, radius);
            d.setAlpha(FULL_OPACITY);
        }
    }

    protected Drawable getDrawable() {
        if (drawableCache.containsKey(getTokenId())) {
            return drawableCache.get(getTokenId());
        }

        if (mDrawable == null) {
            mDrawable = createDrawable();
        }

        if (mDrawable != null)
            drawableCache.put(getTokenId(), mDrawable);

        return mDrawable;
    }

    protected abstract Drawable createDrawable();

    private transient Drawable mDrawable;

}
