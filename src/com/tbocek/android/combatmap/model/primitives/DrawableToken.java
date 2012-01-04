package com.tbocek.android.combatmap.model.primitives;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Base class for tokens that display some sort of drawable.  Provides standard
 * drawing methods and a caching scheme.  Subclasses need mainly to specify
 * how to load the drawable.
 * @author Tim Bocek
 *
 */
public abstract class DrawableToken extends BaseToken {
    /**
     * ID for serialization.
     */
	private static final long serialVersionUID = -4586968232758191016L;

	/**
	 * Alpha value that will draw at full opacity.
	 */
    private static final int FULL_OPACITY = 255;

    /**
     * Alpha value that will draw at half opacity.
     */
    private static final int HALF_OPACITY = 128;

    /**
     * Map between token ID and the the drawable that has been loaded for that
     * token ID, if it exists.  Drawables already in this map will be reused.
     */
    private static Map<String, Drawable> drawableCache
    	= new HashMap<String, Drawable>();

    @Override
    public final void drawBloodied(
    		final Canvas c, final float x, final float y, final float radius,
    		final boolean isManipulatable) {
        Drawable d = getDrawable();
        if (d != null) {
        	d.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(
        			new float[] {1,  0,    0,   0,  0,
        			             0,  .25f, 0,   0,  0,
        			             0,  0,   .25f, 0,  0,
        			             0,  0,    0,   1,  0}
        	)));
            draw(c, x, y, radius, false, isManipulatable);
            d.setColorFilter(null);
        } else {
        	drawPlaceholder(c, x, y, radius);
        }

    }

	/**
	 * Draws a placeholder where this token should be.  Used for when the
	 * token hasn't loaded yet.
	 * @param c Canvas
	 * @param x center x
	 * @param y center y
	 * @param radius circle radius
	 */
	private void drawPlaceholder(final Canvas c, final float x, final float y,
			final float radius) {
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(2.0f);
		c.drawCircle(x, y, radius, p);
	}

    @Override
    public final void draw(
    		final Canvas c, final float x, final float y, final  float radius,
    		final boolean darkBackground, final boolean isManipulatable) {
        Drawable d = getDrawable();
        if (d != null) {
            c.save(Canvas.CLIP_SAVE_FLAG);
            clipToCircle(c, x, y, radius);
            d.setBounds(new Rect((int) (x - radius), (int) (y - radius),
                                     (int) (x + radius), (int) (y + radius)));
            if (!isManipulatable) {
            	d.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(
            			new float[] {.5f, 0,   0,  0,  0,
            			             0,  .5f,  0,  0,  0,
            			             0,  0,   .5f, 0,  0,
            			             0,  0,    0,   1,  0}
            	)));
            }
            d.draw(c);
            if (!isManipulatable) {
            	d.setColorFilter(null);
            }
            c.restore();
        } else {
        	drawPlaceholder(c, x, y, radius);
        }
    }

    /**
     * Sets the clip of the given canvas to a circle centered at (x,y) with
     * radius r.
     * @param c The canvas we are drawing on.
     * @param x X coordinate of the circle to clip to.
     * @param y Y coordinate of the circle to clip to.
     * @param radius Radius of the circle to clip to.
     */
    private void clipToCircle(
    		final Canvas c, final float x, final float y, final float radius) {
        Path p = new Path();
        p.addCircle(x, y, radius, Path.Direction.CW);
        c.clipPath(p);
    }

    @Override
    protected final void drawGhost(
    		final Canvas c, final float x, final float y, final float radius) {
        Drawable d = getDrawable();
        if (d != null) {
            d.setAlpha(HALF_OPACITY);
            draw(c, x, y, radius, false, true);
            d.setAlpha(FULL_OPACITY);
        }
    }

    /**
     * Returns the drawable associated with this token.  This may cause the
     * drawable to be loaded.
     * @return The drawable.
     */
    private Drawable getDrawable() {
        if (drawableCache.containsKey(getTokenId())) {
            return drawableCache.get(getTokenId());
        }
        return mDrawable;
    }

    @Override
    public final boolean needsLoad() {
    	return !drawableCache.containsKey(getTokenId());
    }

    @Override
    public final void load() {
        if (mDrawable == null) {
            mDrawable = createDrawable();
        }

        if (mDrawable != null) {
            drawableCache.put(getTokenId(), mDrawable);
        }
    }

    /**
     * Loads the drawable.  Subclasses override this to specify how to load
     * their specific type of drawable.
     * @return The created drawable, or null if the drawable could not be
     * 		created.
     */
    protected abstract Drawable createDrawable();

    /**
     * The loaded drawable to use.
     */
    private transient Drawable mDrawable;

}