package com.tbocek.android.combatmap.graphicscore;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public final class Util {

	public static float distance(PointF p1, PointF p2) {
		return distance(p1.x, p1.y, p2.x, p2.y);
	}
	
	public static float distance(float x1, float y1, float x2, float y2) {
		return (float)Math.sqrt((double)(x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)); 
	}
	
	public static float degreesToRadians(float deg) {
		return (float) (deg * Math.PI / 180);
	}
	
	public static List<Integer> getStandardColorPalette() {
		List<Integer> palette = new ArrayList<Integer>();
		
		palette.add(Color.WHITE);
		palette.add(Color.LTGRAY);
		palette.add(Color.GRAY);
		palette.add(Color.DKGRAY);
		palette.add(Color.BLACK);
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, 1, 1};
			palette.add(Color.HSVToColor(hsv));
		}
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, .5f, 1};
			palette.add(Color.HSVToColor(hsv));
		}
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, 1, .5f};
			palette.add(Color.HSVToColor(hsv));
		}

		return palette;
	}
}
