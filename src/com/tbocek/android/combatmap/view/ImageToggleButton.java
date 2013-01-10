package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * An ImageButton that stores a toggled state, and allows the toggle to be set.
 * The toggle is not intrinsically set by any logic in the button; client code
 * needs to set it.
 * 
 * @author Tim
 * 
 */
public final class ImageToggleButton extends ImageButton {
    /**
     * The color to use when drawing a border around toggled buttons.
     */
    private static final int TOGGLE_BORDER_COLOR = Color.rgb(0, 127, 255);

    /**
     * Width of the toggle border line.
     */
    private static final int TOGGLE_BORDER_WIDTH = 3;

    /**
     * Whether to draw the button toggled.
     */
    private boolean mToggled = false;

    /**
     * Creates a new ImageToggleButton in the given context.
     * 
     * @param context
     *            Context to use.
     */
    public ImageToggleButton(final Context context) {
        super(context);
    }

    public ImageToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @return Whether the button is toggled.
     */
    public boolean isToggled() {
        return this.mToggled;
    }

    @Override
    public void onDraw(final Canvas c) {
        super.onDraw(c);

        Paint paint = new Paint();
        paint.setColor(TOGGLE_BORDER_COLOR);
        paint.setStrokeWidth(TOGGLE_BORDER_WIDTH);
        paint.setStyle(Style.STROKE);
        if (this.mToggled) {
            c.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
        }
    }

    /**
     * Sets the toggled state.
     * 
     * @param toggled
     *            Whether the button should be toggled.
     */
    public void setToggled(final boolean toggled) {
        this.mToggled = toggled;
        this.invalidate();
    }
}
