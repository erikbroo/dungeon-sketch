package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.Token;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.widget.Button;

/**
 * Represents a button that contains a prototype for a token.  Draws the button based on the token's prototype,
 * and provides a method to construct a copy.
 * @author Tim
 *
 */
public class TokenButton extends Button {
	Token prototype;
	private static CoordinateTransformer DUMMY_TRANSFORMER = new CoordinateTransformer();
	
	public TokenButton(Context context, Token prototype) {
		super(context);
		this.prototype = prototype;
		
		this.setWidth(80);
		this.setHeight(70);
		this.prototype.location =  new PointF(40, 35);
	}
	
	public void onDraw(Canvas c) {
		prototype.draw(c, DUMMY_TRANSFORMER);
	}
	
	public Token getClone() {
		return prototype.clone();
	}


}
