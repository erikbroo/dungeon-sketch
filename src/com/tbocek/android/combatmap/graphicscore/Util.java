package com.tbocek.android.combatmap.graphicscore;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;

/**
 * Random graphics utilities.
 * @author Tim Bocek
 *
 */
public final class Util {
	
	/**
	 * Hue is represented on a 360 degree circle.
	 */
	private static final int HUE_DEGREES = 360;
	
	/**
	 * Amount to increment around the color wheel.
	 */
	private static final int HUE_INCREMENT = 30;
	
	/**
	 * Amount to adjust saturation and luminence by when they are not held at
	 * full when generating the palette.
	 */
	private static final float SAT_LUM_ADJUST = .5f;
	
	/**
	 * Utility class - private constructor.
	 */
	private Util() { }
	
	/**
	 * Compute the distance between two PointFs.
	 * @param p1 First point.
	 * @param p2 Second point.
	 * @return The distance.
	 */
	public static float distance(final PointF p1, final PointF p2) {
		return distance(p1.x, p1.y, p2.x, p2.y);
	}
	
	/**
	 * Compute the distance between two points.
	 * @param x1 First point x.
	 * @param y1 First point y.
	 * @param x2 Second point x.
	 * @param y2 Second point y.
	 * @return The distance.
	 */
	public static float distance(
			final float x1, final float y1, final float x2, final float y2) {
		return (float) Math.sqrt(
				(double) (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)); 
	}
	
	/**
	 * @return A standard color pallete for the application to use.
	 */
	public static List<Integer> getStandardColorPalette() {
		List<Integer> palette = new ArrayList<Integer>();
		
		palette.add(Color.WHITE);
		palette.add(Color.LTGRAY);
		palette.add(Color.GRAY);
		palette.add(Color.DKGRAY);
		palette.add(Color.BLACK);
		
		for (int h = 0; h < HUE_DEGREES; h += HUE_INCREMENT) {
			float [] hsv = {h, 1, 1};
			palette.add(Color.HSVToColor(hsv));
		}
		
		for (int h = 0; h < HUE_DEGREES; h += HUE_INCREMENT) {
			float [] hsv = {h, SAT_LUM_ADJUST, 1};
			palette.add(Color.HSVToColor(hsv));
		}
		
		for (int h = 0; h < HUE_DEGREES; h += HUE_INCREMENT) {
			float [] hsv = {h, 1, SAT_LUM_ADJUST};
			palette.add(Color.HSVToColor(hsv));
		}

		return palette;
	}
}
