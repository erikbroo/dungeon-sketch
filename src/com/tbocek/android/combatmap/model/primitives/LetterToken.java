package com.tbocek.android.combatmap.model.primitives;

import java.util.HashSet;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * This class represents a token that draws as a capital letter
 * inside a circle.
 *
 * @author Tim Bocek
 *
 */
public final class LetterToken extends BaseToken {

	/**
	 * The ID for serialization.
	 */
	private static final long serialVersionUID = -8395100608110965181L;

	/**
	 * The stroke width to use when drawing this token.
	 */
	private static final int STROKE_WIDTH = 3;

	/**
     * The letter to draw in the circle.  While this could be anything, it
     * should really only be a single character.
     */
    private String mLetter;

    /**
     * Constructor.
     * @param letter The single character to draw in the circle.
     */
    public LetterToken(final String letter) {
        this.mLetter = letter;
    }

    @Override
    public BaseToken clone() {
        return copyAttributesTo(new LetterToken(mLetter));
    }

    @Override
    public void drawBloodied(final Canvas c, final float x, final float y,
    		final float radius, final boolean isManipulatable) {
        Paint p = new Paint();
        p.setColor(isManipulatable ? Color.RED : Color.rgb(255, 128, 128));
        draw(c, x, y, radius, p);
    }

    @Override
    public void draw(final Canvas c, final float x, final float y,
    		final float radius, final boolean darkBackground,
    		final boolean isManipulatable) {
        Paint p = new Paint();
        p.setColor(isManipulatable
        		? (darkBackground ? Color.WHITE : Color.BLACK)
        		: Color.GRAY);
        draw(c, x, y, radius, p);
    }

    /**
     * Draws the token with the given paint style.
     * @param c Canvas to draw on.
     * @param x X coordinate of the token center, in screen space.
     * @param y Y coordinate of the token center, in screen space.
     * @param radius Radius of the token, in screen space.
     * @param paint Paint object to use when drawing the circle and text.
     */
    private void draw(final Canvas c, final float x, final float y,
    		final float radius, final Paint paint) {
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setStyle(Style.STROKE);
        c.drawCircle(x, y, radius, paint);
        paint.setTextSize(radius);
        paint.setStrokeWidth(2);
        paint.setStyle(Style.FILL);
        c.drawText(mLetter, x - radius / 4, y + radius / 4, paint);
    }

    @Override
    public void drawGhost(final Canvas c, final float x, final float y,
    		final float radius) {
        //TODO(tim.bocek): Make this look different
        Paint p = new Paint();
        p.setColor(Color.GRAY);
        draw(c, x, y, radius, p);
    }

    @Override
    protected String getTokenClassSpecificId() {
        return mLetter;
    }

    @Override
    public Set<String> getDefaultTags() {
        Set<String> s = new HashSet<String>();
        s.add("built-in");
        s.add("letter");
        return s;
    }
}
