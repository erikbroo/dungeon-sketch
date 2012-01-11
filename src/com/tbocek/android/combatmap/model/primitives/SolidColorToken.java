package com.tbocek.android.combatmap.model.primitives;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * A built-in token type that draws as a solid color.
 *
 * @author Tim Bocek
 *
 */
public final class SolidColorToken extends BaseToken {

	private static final DecimalFormat SORT_ORDER_FORMAT =
		new DecimalFormat("#0000.###");

	/**
     * This token's color.
     */
    private int mColor;

    /**
     * A sort order for the token, since sorting on color does not produce a
     * pleasing ordering to the tokens.
     */
    private int mSortOrder;

    /**
     * Constructor.
     * @param c The color to draw this token with.
     * @param sortOrder Ordering for this token.  We manually specify this
     * 		because sorting on color doesn't produce good-looking results.
     */
    public SolidColorToken(final int c, final int sortOrder) {
        this.mColor = c;
        this.mSortOrder  = sortOrder;
    }

    /**
     * Draws an indication of a past location of the token.
     * @param c
     * @param transformer
     * @param ghostPoint Location to draw the ghost, in world space
     */
    @Override
    public void drawGhost(final Canvas c, final float x, final float y, final float radius) {
        Paint p = new Paint();
        p.setStrokeWidth(2);
        p.setColor(mColor);
        p.setStyle(Style.STROKE);
        c.drawCircle(x, y, radius, p);
    }

    @Override
    public void draw(final Canvas c, final float x, final float y,
    		final float radius, final boolean darkBackground,
    		final boolean isManipulatable) {
    	// TODO: Respect whether the token is manipulatable.
        Paint p = new Paint();
        p.setColor(mColor);
        c.drawCircle(x, y, radius, p);
    }

    @Override
    public void drawBloodied(final Canvas c, final float x, final float y, final float radius, final boolean isManipulatable) {
        draw(c, x, y, radius, false, isManipulatable);

        Paint p = new Paint();
        // If token is already colored red, use a dark red border so it's visible
        p.setColor(mColor != Color.RED ? Color.RED : Color.rgb(127, 0, 0));
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(8);
        c.drawCircle(x, y, radius - 4, p);
    }

    @Override
    public BaseToken clone() {
        return copyAttributesTo(new SolidColorToken(mColor, mSortOrder));
    }

    @Override
    protected String getTokenClassSpecificId() {
    	SORT_ORDER_FORMAT.setDecimalSeparatorAlwaysShown(false);
        return SORT_ORDER_FORMAT.format(mSortOrder)
        		+ '_' + Integer.toString(mColor);
    }

    @Override
    public Set<String> getDefaultTags() {
        Set<String> s = new HashSet<String>();
        s.add("built-in");
        s.add("solid color");
        return s;
    }
}
