package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.model.primitives.BaseToken;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;

/**
 * Represents a button that contains a prototype for a token.  Draws the button
 * based on the token's prototype, and provides a method to construct a copy.
 * @author Tim Bocek
 *
 */
public class TokenButton extends ImageView {

	/**
	 * How much to scale the token by.  1.0 means it is completely inscribed in
	 * the button element.
	 */
	private static final float TOKEN_SCALE = 0.8f;

	/**
	 * The token represented by this button.
	 */
    private BaseToken mPrototype;

    /**
     * A gesture detector used to detect long presses for drag and drop start.
     */
    private GestureDetector gestureDetector;

    /**
     * A gesture listener used to start a drag and drop when a long press
     * occurs.
     */
    private SimpleOnGestureListener gestureListener =
    	new SimpleOnGestureListener() {
        public void onLongPress(final MotionEvent e) {
        	if (android.os.Build.VERSION.SDK_INT
        			>= android.os.Build.VERSION_CODES.HONEYCOMB)
        	TokenButton.this.onStartDrag();
        }
    };

    /**
     * Whether tokens should be drawn as if on a dark background.
     */
	private boolean drawDark = false;

    /**
     * Called when a drag and drop operation should start.
     */
    protected void onStartDrag() {
        startDrag(null, new View.DragShadowBuilder(TokenButton.this),
      		  mPrototype.clone(), 0);
    }

	/**
     * Constructor.
     * @param context The context to create this view in.
     * @param prototype The prototype token that this view represents.
     */
    public TokenButton(final Context context, final BaseToken prototype) {
        super(context);
        this.mPrototype = prototype;

        //Set up listener to see if a drag has started.
        gestureDetector =
        	new GestureDetector(this.getContext(), gestureListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    @Override
    public void onDraw(final Canvas c) {
        mPrototype.draw(c, (float) this.getWidth() / 2,
        		(float) this.getHeight() / 2,
        		getTokenRadius(),
        		drawDark, true);
    }

	/**
	 * @return The radius that should be used when drawing a token.
	 */
	protected final float getTokenRadius() {
		return Math.min(this.getWidth(), this.getHeight()) * TOKEN_SCALE / 2;
	}

    /**
     * Gets a new token that is a clone of the token specified here.
     * @return A clone of the token.
     */
    public final BaseToken getClone() {
        return mPrototype.clone();
    }

    /**
     * Gets the token ID of the managed token.
     * @return The token ID.
     */
    public final String getTokenId() {
    	return mPrototype.getTokenId();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        this.gestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

	/**
	 * @param drawDark Whether tokens are drawn on a dark background.
	 */
	public void setShouldDrawDark(boolean drawDark) {
		this.drawDark = drawDark;
	}


	/**
	 * @return Whether tokens are drawn on a dark background.
	 */
	public boolean shouldDrawDark() {
		return drawDark;
	}
}
