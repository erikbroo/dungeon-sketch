package com.tbocek.android.combatmap.graphicscore;

import android.graphics.PointF;

public class CoordinateTransformer {
	public float zoomLevel = 1.0f;
	public float originX = 0.0f;
	public float originY = 0.0f;

	public PointF worldSpaceToScreenSpace(PointF wscoord) {
		return new PointF(zoomLevel * wscoord.x + originX, zoomLevel * wscoord.y + originY);
	}
	
	public PointF screenSpaceToWorldSpace(PointF wscoord) {
		return new PointF((wscoord.x - originX) / zoomLevel, (wscoord.y - originY) / zoomLevel);
	}
	
	public float worldSpaceToScreenSpace(float d) {
		return d * zoomLevel;
	}
	
	public float screenSpaceToWorldSpace(float d) {
		return d / zoomLevel;
	}
}