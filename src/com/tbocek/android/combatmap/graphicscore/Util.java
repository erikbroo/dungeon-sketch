package com.tbocek.android.combatmap.graphicscore;

import android.graphics.PointF;

public final class Util {

	public static float distance(PointF p1, PointF p2) {
		return (float)Math.sqrt((double)(p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y)); 
	}
}
