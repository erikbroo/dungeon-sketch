package com.tbocek.android.combatmap.graphicscore;

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
     * The letter to draw in the circle.  While this could be anything, it
     * should really only be a single character.
     */
    private String letter;

    /**
     * Constructor.
     * @param letter The single character to draw in the circle.
     */
    public LetterToken(final String letter) {
        this.letter = letter;
    }

    @Override
    public BaseToken clone() {
        return new LetterToken(letter);
    }

    @Override
    public void drawBloodied(final Canvas c, final float x, final float y, final float radius) {
        Paint p = new Paint();
        p.setColor(Color.RED);
        draw(c, x, y, radius, p);
    }

    @Override
    public void draw(final Canvas c, final float x, final float y, final float radius) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
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
    private void draw(final Canvas c, final float x, final float y, final float radius, final Paint paint) {
        paint.setStrokeWidth(3);
        paint.setStyle(Style.STROKE);
        c.drawCircle(x, y, radius, paint);
        paint.setTextSize(radius);
        paint.setStrokeWidth(2);
        paint.setStyle(Style.FILL);
        c.drawText(letter, x-radius/4, y+radius/4, paint);
    }

    @Override
    public void drawGhost(final Canvas c, final float x, final float y, final float radius) {
        //TODO(tim.bocek): Make this look different
        Paint p = new Paint();
        p.setColor(Color.GRAY);
        draw(c, x, y, radius, p);
    }

    @Override
    protected String getTokenClassSpecificId() {
        return letter;
    }
    
    @Override
    public Set<String> getDefaultTags() {
        Set<String> s = new HashSet<String>();
        s.add("built-in");
        s.add("letter");
        return s;
    }
}
