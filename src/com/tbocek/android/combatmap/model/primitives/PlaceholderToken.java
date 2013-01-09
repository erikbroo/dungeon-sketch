package com.tbocek.android.combatmap.model.primitives;

import java.util.Set;

import com.tbocek.android.combatmap.TokenDatabase;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * This token is meant to be replaced by another token after the token database
 * loads.
 * @author Tim
 *
 */
public class PlaceholderToken extends BaseToken {
	
	/**
	 * The ID of the token that this is a placeholder for.
	 */
	private String mReplaceWith;
	
	/**
	 * Constructor.
	 * @param tokenId The ID of the token that this is a placeholder for.
	 */
	public PlaceholderToken(String tokenId) {
		mReplaceWith = tokenId;
	}

	@Override
	public BaseToken clone() {
		return new PlaceholderToken(mReplaceWith);
	}

	@Override
	protected void drawImpl(Canvas c, float x, float y, float radius,
			boolean darkBackground, boolean isManipulatable) {
		Paint p = new Paint();
		p.setStyle(Paint.Style.STROKE);
		p.setColor(Color.BLACK);
		p.setStrokeWidth(1.0f);
		
		c.drawCircle(x, y, radius, p);
	}

	@Override
	protected void drawBloodiedImpl(Canvas c, float x, float y, float radius,
			boolean isManipulatable) {
		drawImpl(c, x, y, radius, true, true);
		
	}

	@Override
	protected void drawGhost(Canvas c, float x, float y, float radius) {
		drawImpl(c, x, y, radius, true, true);
		
	}

	@Override
	protected String getTokenClassSpecificId() {
		return mReplaceWith;
	}

	@Override
	public Set<String> getDefaultTags() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BaseToken deplaceholderize(TokenDatabase database) {
		return database.createToken(this.mReplaceWith.replace(this.getClass().getSimpleName(), ""));
	}
}
