package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;


/**
 * Encapsulates a set of tokens that have been placed on the grid.
 * @author Tim Bocek
 *
 */
public final class TokenCollection implements Serializable {
	/**
	 * ID for serialization.
	 */
    private static final long serialVersionUID = 4852258096470549968L;

    /**
     * The tokens that have been added to the grid.
     */
    private List<BaseToken> tokens = new ArrayList<BaseToken>();

    /**
     * Given a point in screen space, returns the token under that point.
     * @param p The point in screen space.
     * @param transformer Grid space to screen space transformation.
     * @return The token under the point, or null if no tokens.
     */
    public BaseToken getTokenUnderPoint(
    		final PointF p, final CoordinateTransformer transformer) {
        for (int i = 0; i < tokens.size(); ++i) {
        	float distance =
        		Util.distance(
        				p, transformer.worldSpaceToScreenSpace(
        						tokens.get(i).getLocation()));
            if (distance < transformer.worldSpaceToScreenSpace(
            		tokens.get(i).getSize() / 2)) {
                return tokens.get(i);
            }
        }
        return null;
    }

    /**
     * Adds a token to the collection.
     * @param t The token to add.
     */
    public void addToken(final BaseToken t) {
        tokens.add(t);
    }

    /**
     * Removes all tokens from the collection.
     */
    public void clear() {
        tokens.clear();
    }

    /**
     * Removes a token from the collection.
     * @param t The token to remove.
     */
    public void remove(final BaseToken t) {
        tokens.remove(t);
    }

    /**
     * Finds a location on the given grid to place this token, and places the
     * token there.  This allows the token to snap to the grid.
     * @param t The token to place.
     * @param attemptedPoint The location where this token should try to be
     * 		placed.
     * @param grid The grid to snap to.
     */
    public void placeTokenNearby(
    		final BaseToken t, final PointF attemptedPoint, final Grid grid) {
        int attemptedDistance = 0;
        PointF point = attemptedPoint;
        // Continually increment the attempted distance until an open grid space
        // is found.  This is guaranteed to succeed. Note that there are some
        // inefficiencies here (the center point is tried four times, each
        // corner of a square is tried twice, etc).  I don't care.  This runs
        // fast enough for reasonable token collections on screen.
        point = grid.getNearestSnapPoint(point, t.getSize());
        while (true) {
            // Go clockwise around the size of a square centered on the
        	// originally attempted point and with sized of
        	// length attemptedDistance*2

            //Across the top
        	// The -attemptedDistance + 1 ensures a nice spiral pattern
            for (int i = -attemptedDistance + 1; i <= attemptedDistance; ++i) {
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

    /**
     * Tries to place the token at the specified location.  If a token is
     * already here, returns False.  If not, sets the tokens location and
     * returns True.
     * @param t The token to try to place.
     * @param point The location at which to try to place the token.
     * @return True if successfully placed.
     */
    private boolean tryToPlaceHere(final BaseToken t, final PointF point) {
        if (isLocationUnoccupied(point, t.getSize() / 2)) {
            t.setLocation(point);
            return true;
        }
        return false;
    }

    /**
     * Checks whether placing a token with the given radius at the give location
     * would intersect any other tokens.
     * @param point Center of the token to try placing here.
     * @param radius Radius of the token to try placing here.
     * @return True if the location is unoccupied, False if there would be an
     * 		intersection.
     */
    private boolean isLocationUnoccupied(
    		final PointF point, final double radius) {
        for (BaseToken t : tokens) {
            if (Util.distance(point, t.getLocation())
            		< radius + t.getSize() / 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Draws all tokens.
     * @param canvas The canvas to draw on.
     * @param transformer Transformer from grid space to screen space.
     * @param isDark Whether to draw as if on a dark background.
     */
    public void drawAllTokens(
    		final Canvas canvas, final CoordinateTransformer transformer,
    		boolean isDark) {
        for (int i = 0; i < tokens.size(); ++i) {
        	// TODO: Take advantage of knowing whether we have a dark
        	// background.
            tokens.get(i).drawInPosition(canvas, transformer, isDark);
        }

    }

    /**
     * Computes and returns a bounding rectangle that can contain all the
     * tokens.
     * @return The bounding rectangle.
     */
    public BoundingRectangle getBoundingRectangle() {
    	BoundingRectangle r = new BoundingRectangle();
        for (BaseToken t : tokens) {
            r.updateBounds(t.getBoundingRectangle());
        }
        return r;
    }

    /**
     * Returns whether there are tokens in this collection.
     * @return True if collection is empty.
     */
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

}
