package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;

public class CoordinateTransformer implements Serializable {
	private static final long serialVersionUID = -336836518697184615L;
	
	// Conversion of lengths in world space to lengths in screen space
	private float zoomLevel = 1.0f;
	
	// Origin, representing the U-L screen corner's position in screen space (NOT world space!)
	private float originX = 0.0f;
	private float originY = 0.0f;

	
	public CoordinateTransformer(float originX, float originY, float zoomLevel) {
		this.originX = originX;
		this.originY = originY;
		this.zoomLevel = zoomLevel;
	}
	
	/**
	 * Changes the scale of the transformation
	 * @param scaleFactor Amount to change the zoom level by
	 * @param invariant Screen space point that should map to the same world space
	 * 		point before and after the transformation.
	 */
	public void zoom(float scaleFactor, PointF invariant) {
		float lastZoomLevel = zoomLevel;
		float lastOriginX = originX;
		float lastOriginY = originY;
		
		zoomLevel *= scaleFactor;
		
		// Change the origin so that we zoom around the focus point.
		// Derived by assuming that the focus point should map to the same point in world space before and after the zoom.
		originX = invariant.x - (invariant.x - lastOriginX) * zoomLevel / lastZoomLevel;
		originY = invariant.y - (invariant.y - lastOriginY) * zoomLevel / lastZoomLevel;
		
	}
	
	public PointF worldSpaceToScreenSpace(PointF wscoord) {
		return worldSpaceToScreenSpace(wscoord.x, wscoord.y);
	}
	
	public PointF screenSpaceToWorldSpace(PointF sscoord) {
		return screenSpaceToWorldSpace(sscoord.x, sscoord.y);
	}
	
	
	
	public PointF worldSpaceToScreenSpace(float x, float y) {
		return new PointF(zoomLevel * x + originX, zoomLevel * y + originY);
	}
	
	public PointF screenSpaceToWorldSpace(float x, float y) {
		return new PointF((x - originX) / zoomLevel, (y - originY) / zoomLevel);
	}
	
	public void setOriginInWorldSpace(float x, float y) {
		originX = x * zoomLevel;
		originY = y * zoomLevel;
	}
	
	public float worldSpaceToScreenSpace(float d) {
		return d * zoomLevel;
	}
	
	public float screenSpaceToWorldSpace(float d) {
		return d / zoomLevel;
	}
	
	public CoordinateTransformer compose(CoordinateTransformer second) {
		return new CoordinateTransformer(
				second.worldSpaceToScreenSpace(originX) + second.originX,
				second.worldSpaceToScreenSpace(originY) + second.originY,
				zoomLevel * second.zoomLevel);
	}

	public PointF getOrigin() {
		return new PointF(originX, originY);
	}
	
	public void moveOrigin(float dx, float dy) {
		originX += dx;
		originY += dy;
	}

	public void setZoom(float zoomLevel) {
		this.zoomLevel = zoomLevel;
		
	}
}