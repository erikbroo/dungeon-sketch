package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.PointF;

import android.content.Context;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Represents a button that contains a prototype for a token.  Draws the button based on the token's prototype,
 * and provides a method to construct a copy.
 * @author Tim
 *
 */
public class TokenButton extends ImageView {
	BaseToken prototype;
	private GestureDetector gestureDetector;
	
	private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		public void onLongPress(MotionEvent e) {
			//TODO(tbocek): StartDrag
			startDrag(null, new View.DragShadowBuilder(TokenButton.this), prototype.clone(), 0);
		}
	};
	
	public TokenButton(Context context, BaseToken prototype) {
		super(context);
		this.prototype = prototype;
		
		//Set up listener to see if a drag has started.
		gestureDetector = new GestureDetector(this.getContext(), gestureListener);

		this.prototype.setLocation(new PointF(40, 35));
	}
	
	public void onDraw(Canvas c) {
		prototype.draw(c, (float)this.getWidth()/2, (float)this.getHeight()/2, Math.min(this.getWidth(), this.getHeight()) * .8f / 2);
	}
	
	public BaseToken getClone() {
		return prototype.clone();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		this.gestureDetector.onTouchEvent(ev);
		return super.onTouchEvent(ev);
	}


}
