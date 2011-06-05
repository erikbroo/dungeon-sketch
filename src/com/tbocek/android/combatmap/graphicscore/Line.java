package com.tbocek.android.combatmap.graphicscore;

import java.util.ArrayList;
import java.util.List;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Encapsulates a single vector-drawn line.
 * @author Tim
 *
 */
public class Line {
	public Line(int color) {
		this.color = color;
	}
	private static float MIN_POINT_DISTANCE = 2;
	private int color = Color.BLACK;
	private int width = 2;
	
	private BoundingRectangle boundingRectangle = new BoundingRectangle();
	private List<PointF> points = new ArrayList<PointF>();
	private List<Boolean> shouldDraw = new ArrayList<Boolean>();

	public void addPoint(PointF p) {
		if (points.size() == 0 || isFarEnoughFromLastPoint(p)) {
			points.add(p);
			shouldDraw.add(true);
			boundingRectangle.updateBounds(p);
		}
	}
	
	private boolean isFarEnoughFromLastPoint(PointF p) {
		return Util.distance(p, points.get(points.size() - 1)) > MIN_POINT_DISTANCE;
	}
	
	
	public void draw(Canvas c, CoordinateTransformer transformer) {
		//Do not try to draw a line with too few points.
		if (points.size() < 2) return;
		
		Paint paint = new Paint();
		paint.setColor(color);
		paint.setStrokeWidth(width);
		
		for (int i = 0; i < points.size() - 1; ++i) {
			if (shouldDraw.get(i).booleanValue() && shouldDraw.get(i+1).booleanValue()) {
				PointF p1 = transformer.worldSpaceToScreenSpace(points.get(i));
				PointF p2 = transformer.worldSpaceToScreenSpace(points.get(i+1));
				c.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
			}
		}
	}
	
	public void erase(PointF center, float radius) {
		if (boundingRectangle.intersectsWithCircle(center, radius)) {
			for (int i = 0; i < points.size(); ++i) {
				if (Util.distance(center, points.get(i)) < radius) {
					shouldDraw.set(i, false);
				}
			}
		}
	}
}
