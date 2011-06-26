package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;


public class TokenCollection implements Serializable {
	private static final long serialVersionUID = 4852258096470549968L;
	
	private List<BaseToken> tokens = new ArrayList<BaseToken>();

	public BaseToken getTokenUnderPoint(PointF p, CoordinateTransformer transformer) {
		for (int i=0;i<tokens.size();++i) {
			if (Util.distance(p, transformer.worldSpaceToScreenSpace(tokens.get(i).getLocation())) < transformer.worldSpaceToScreenSpace(tokens.get(i).getSize() / 2 )) {
				return tokens.get(i);
			}
		}
		return null;
	}

	public void addToken(BaseToken t) {
		tokens.add(t);
	}

	public void clear() {
		tokens.clear();
	}

	public void remove(BaseToken t) {
		tokens.remove(t);
	}
	
	//NOTE: Everything in grid space.
	public void placeTokenNearby(BaseToken t, PointF point, Grid grid) {
		int attemptedDistance = 0;
		// Continually increment the attempted distance until an open grid space is found.  This is guaranteed to succeed.
		// Note that there are some inefficiencies here (the center point is tried four times, each corner of a square is tried
		// twice, etc).  I don't care.  This runs fast enough for reasonable token collections on screen.
		point = grid.getNearestSnapPoint(point, t.getSize());
		while (true) {
			// Go clockwise around the size of a square centered on the originally attempted point and
			// with sized of length attemptedDistance*2
			
			//Across the top
			for (int i = -attemptedDistance + 1; i <= attemptedDistance; ++i) { // The -attemptedDistance + 1 ensures a nice spiral pattern
				if (tryToPlaceHere(t, new PointF(point.x + i, point.y - attemptedDistance))) return;
			}
			
			//Down the right
			for (int i = -attemptedDistance; i <= attemptedDistance; ++i) {
				if (tryToPlaceHere(t, new PointF(point.x + attemptedDistance, point.y + i))) return;
			}

			//Across the bottom
			for (int i = attemptedDistance; i >= -attemptedDistance; --i) {
				if (tryToPlaceHere(t, new PointF(point.x + i, point.y + attemptedDistance))) return;
			}
			
			//Up the left
			for (int i = attemptedDistance; i >= -attemptedDistance; --i) {
				if (tryToPlaceHere(t, new PointF(point.x - attemptedDistance, point.y + i))) return;
			}
			attemptedDistance++;
		}
	}
	
	private boolean tryToPlaceHere(BaseToken t, PointF point) {
		if (isLocationUnoccupied(point, t.getSize() / 2)) {
			t.setLocation(point);
			return true;
		}
		return false;
	}
	
	private boolean isLocationUnoccupied(PointF point, double radius) {
		for (BaseToken t : tokens) {
			if (Util.distance(point, t.getLocation()) < radius + t.getSize() / 2) {
				return false;
			}
		}
		return true;
	}

	public void drawAllTokens(Canvas canvas, CoordinateTransformer transformer) {
		for (int i = 0; i < tokens.size(); ++i){
			tokens.get(i).drawInPosition(canvas, transformer);
		}
		
	}

	public List<BaseToken> getTokens() {
		return tokens;
	}

	public boolean isEmpty() {
		return tokens.isEmpty();
	}
	
}
