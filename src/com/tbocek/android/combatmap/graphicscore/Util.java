package com.tbocek.android.combatmap.graphicscore;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

public final class Util {

	public static float distance(PointF p1, PointF p2) {
		return (float)Math.sqrt((double)(p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y)); 
	}
	
	public static float degreesToRadians(float deg) {
		return (float) (deg * Math.PI / 180);
	}
	
	public static List<Integer> getStandardColorPalette() {
		List<Integer> palette = new ArrayList<Integer>();
		
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
		
		palette.add(Color.WHITE);
		palette.add(Color.LTGRAY);
		palette.add(Color.GRAY);
		palette.add(Color.DKGRAY);
		palette.add(Color.BLACK);
		
		return palette;
	}
}
