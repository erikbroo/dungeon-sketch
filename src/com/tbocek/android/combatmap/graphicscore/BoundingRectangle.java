package com.tbocek.android.combatmap.graphicscore;

import android.graphics.PointF;

public class BoundingRectangle {
	private float boundsXMin = Float.MAX_VALUE;
	private float boundsXMax = Float.MIN_VALUE;
	private float boundsYMin = Float.MAX_VALUE;
	private float boundsYMax = Float.MIN_VALUE;
	
	public void updateBounds(PointF p) {
		this.boundsXMin = Math.min(this.boundsXMin, p.x);
		this.boundsXMax = Math.max(this.boundsXMax, p.x);
		this.boundsYMin = Math.min(this.boundsYMin, p.y);
		this.boundsYMax = Math.max(this.boundsYMax, p.y);
	}
	
	public boolean contains(PointF p) {
		return p.x >= boundsXMin && p.x <= boundsXMax &&
		       p.y >= boundsYMin && p.y <= boundsYMax;
	}
	
	public boolean intersectsWithCircle(PointF center, float radius) {
		return center.x + radius >= boundsXMin && center.x - radius <= boundsXMax &&
			   center.y + radius >= boundsYMin && center.y - radius <= boundsYMax;
	}

}