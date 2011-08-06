package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;

import android.graphics.Canvas;

/**
 * Defines a transformation from one 2D coordinate system to another coordinate
 * system.
 * The class is written in terms of a world space/screen space conversion.
 * @author Tim Bocek
 *
 */
public final class CoordinateTransformer implements Serializable {

	/**
	 * ID for serialization.
	 */
    private static final long serialVersionUID = -336836518697184615L;

    /**
     * Conversion of lengths in world space to lengths in screen space.
     */
    private float mZoomLevel = 1.0f;

    /**
     * Upper-left corner's x coordinate in screen space.
     */
    private float mOriginX = 0.0f;

    /**
     * Upper-left corner's y coordinate in screen space.
     */
    private float mOriginY = 0.0f;


    /**
     * Constructor.
     * @param originX X coordinate of the screen space origin.
     * @param originY Y coordinate of the screen space origin.
     * @param zoomLevel Conversion of world space to screen space lengths.
     */
    public CoordinateTransformer(
    		final float originX, final float originY, final float zoomLevel) {
        this.mOriginX = originX;
        this.mOriginY = originY;
        this.mZoomLevel = zoomLevel;
    }

    /**
     * Changes the scale of the transformation.
     * @param scaleFactor Amount to change the zoom level by
     * @param invariant Screen space point that should map to the same world
     * 		space point before and after the transformation.
     */
    public void zoom(final float scaleFactor, final PointF invariant) {
        float lastZoomLevel = mZoomLevel;
        float lastOriginX = mOriginX;
        float lastOriginY = mOriginY;

        mZoomLevel *= scaleFactor;

        // Change the origin so that we zoom around the focus point.
        // Derived by assuming that the focus point should map to the same point
        // in world space before and after the zoom.
        mOriginX = invariant.x
        	- (invariant.x - lastOriginX) * mZoomLevel / lastZoomLevel;
        mOriginY = invariant.y
        	- (invariant.y - lastOriginY) * mZoomLevel / lastZoomLevel;

    }

    /**
     * Converts the given point in world space to screen space.
     * @param wscoord The coordinate in world space.
     * @return The coordinate in screen space.
     */
    public PointF worldSpaceToScreenSpace(final PointF wscoord) {
        return worldSpaceToScreenSpace(wscoord.x, wscoord.y);
    }

    /**
     * Converts the given point in screen space to world space.
     * @param sscoord The coordinate in screen space.
     * @return The coordinate in world space.
     */
    public PointF screenSpaceToWorldSpace(final PointF sscoord) {
        return screenSpaceToWorldSpace(sscoord.x, sscoord.y);
    }

    /**
     * Converts the given point in world space to screen space.
     * @param x X coordinate in world space.
     * @param y Y coordinate in world space.
     * @return The coordinate in screen space.
     */
    public PointF worldSpaceToScreenSpace(final float x, final float y) {
        return new PointF(mZoomLevel * x + mOriginX, mZoomLevel * y + mOriginY);
    }

    /**
     * Converts the given point in screen space to world space.
     * @param x X coordinate in screen space.
     * @param y Y coordinate in screen space.
     * @return The coordinate in world space.
     */
    public PointF screenSpaceToWorldSpace(final float x, final float y) {
        return new PointF(
        		(x - mOriginX) / mZoomLevel, (y - mOriginY) / mZoomLevel);
    }

    /**
     * Sets the origin to the given point in world space.
     * @param x The new origin in world space, x coordinate.
     * @param y The new origin in world space, y coordinate.
     */
    public void setOriginInWorldSpace(final float x, final float y) {
        mOriginX = x * mZoomLevel;
        mOriginY = y * mZoomLevel;
    }

    /**
     * Converts the given distance in world space to screen space.
     * @param d Distance in world space.
     * @return Distance in screen space.
     */
    public float worldSpaceToScreenSpace(final float d) {
        return d * mZoomLevel;
    }

    /**
     * Converts the given distance in screen space to world space.
     * @param d Distance in screen space.
     * @return Distance in world space.
     */
    public float screenSpaceToWorldSpace(final float d) {
        return d / mZoomLevel;
    }

    /**
     * Given a mapping between (x, y) -> (x', y') and a mapping between
     * (x', y') -> (x'', y''), returns a CoordinateTransformer that maps
     * (x, y) -> (x'', y'').
     * @param second The other transformation to compose this transformation
     * 		with.
     * @return The composed transformation.
     */
    public CoordinateTransformer compose(final CoordinateTransformer second) {
        return new CoordinateTransformer(
                second.worldSpaceToScreenSpace(mOriginX) + second.mOriginX,
                second.worldSpaceToScreenSpace(mOriginY) + second.mOriginY,
                mZoomLevel * second.mZoomLevel);
    }

    /**
     * Returns the upper-left-hand corner of the screen in screen space.
     * @return The origin.
     */
    public PointF getOrigin() {
        return new PointF(mOriginX, mOriginY);
    }

    /**
     * Moves the origin by the specified amount.
     * @param dx Amount to move in x dimension.
     * @param dy Amount to move in y dimension.
     */
    public void moveOrigin(final float dx, final float dy) {
        mOriginX += dx;
        mOriginY += dy;
    }

    /**
     * Sets the zoom level (mapping between lengths in wold to lengths in screen
     * space).
     * @param zoomLevel The new zoom level.
     */
    public void setZoom(final float zoomLevel) {
        this.mZoomLevel = zoomLevel;

    }

    public void setMatrix(Canvas c) {
    	c.translate(mOriginX, mOriginY);
    	c.scale(mZoomLevel, mZoomLevel);

    }
}