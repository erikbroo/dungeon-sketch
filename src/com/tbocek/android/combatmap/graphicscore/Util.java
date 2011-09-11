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

    public static class IntersectionPair {

    	public IntersectionPair(PointF i1, PointF i2) {
    		intersection1 = i1;
    		intersection2 = i2;
    	}

    	public PointF intersection1;
    	public PointF intersection2;
    }

    public static IntersectionPair lineCircleIntersection(PointF p1, PointF p2, PointF center, float radius) {
    	// First, make sure that the line segment is anywhere near the circle.
    	if (center.x + radius < Math.min(p1.x, p2.x)
            || center.x - radius > Math.max(p1.x, p2.x)
            || center.y + radius < Math.min(p1.y, p2.y)
            || center.y - radius > Math.max(p1.y, p2.y) )
    	{
    		return null;
    	}

		// Formula from:
		// http://mathworld.wolfram.com/Circle-LineIntersection.html

		// Transform to standard coordinate system where circle is
		// centered at (0, 0)
		float x1 = p1.x - center.x;
		float y1 = p1.y - center.y;
		float x2 = p2.x - center.x;
		float y2 = p2.y - center.y;

		float dx = x2 - x1;
		float dy = y2 - y1;
		float dsquared = dx * dx + dy * dy;
		float det = x1 * y2 - x2 * y1;
		float discriminant = radius * radius * dsquared - det * det;

		// Intersection if the discriminant is non-negative.  In this
		// case we move on and do even more complicated stuff to erase part of
		// the line.
		if (discriminant > 0) {
			float sign_dy = dy < 0 ? -1 : 1;
			// Now, compute the x coordinates of the real intersection with the
			// line, not the line segment.  Again, this is from Wolfram.
			float intersect1X = (float) ((det * dy - sign_dy * dx * Math.sqrt(discriminant))/dsquared) + center.x;
			float intersect2X = (float) ((det * dy + sign_dy * dx * Math.sqrt(discriminant))/dsquared) + center.x;

			// TODO: Only compute these if the line is sufficiently vertical so
			// as to need the Y coordinate to compute the parameterization.
			float intersect1Y = (float) ((det * dx - Math.abs(dy) * Math.sqrt(discriminant))/dsquared) + center.y;
			float intersect2Y = (float) ((det * dx + Math.abs(dy) * Math.sqrt(discriminant))/dsquared) + center.y;

			return new IntersectionPair(
					new PointF(intersect1X, intersect1Y),
					new PointF(intersect2X, intersect2Y));
		} else {
			return null;
		}
    }
}
