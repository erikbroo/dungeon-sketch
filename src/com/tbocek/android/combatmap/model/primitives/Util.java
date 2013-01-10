package com.tbocek.android.combatmap.model.primitives;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

/**
 * Random graphics utilities.
 * 
 * @author Tim Bocek
 * 
 */
public final class Util {

    /**
     * Error margin to use when comparing floating point numbers.
     */
    public static final float FP_COMPARE_ERROR = 1e-10f;

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
     * Constant for full opacity alpha value.
     */
    public static final int FULL_OPACITY = 255;

    /**
     * Constant for half opacity alpha value.
     */
    public static final int HALF_OPACITY = 128;

    /**
     * Color to highlight text with when a token is being dragged to it. Should
     * match Android's Holo light blue.
     */
    public static final int ICS_BLUE = Color.rgb(44, 169, 210);

    /**
     * Compute the distance between two PointFs.
     * 
     * @param p1
     *            First point.
     * @param p2
     *            Second point.
     * @return The distance.
     */
    public static float distance(final PointF p1, final PointF p2) {
        return distance(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Compute the distance between two points.
     * 
     * @param x1
     *            First point x.
     * @param y1
     *            First point y.
     * @param x2
     *            Second point x.
     * @param y2
     *            Second point y.
     * @return The distance.
     */
    public static float distance(final float x1, final float y1,
            final float x2, final float y2) {
        return (float) Math.sqrt((double) (x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));
    }

    /**
     * @return A standard color pallete for the application to use.
     */
    public static List<Integer> getStandardColorPalette() {
        List<Integer> palette = new ArrayList<Integer>();

        palette.add(Color.BLACK);
        palette.add(Color.DKGRAY);
        palette.add(Color.GRAY);
        palette.add(Color.LTGRAY);
        palette.add(Color.WHITE);

        for (int h = 0; h < HUE_DEGREES; h += HUE_INCREMENT) {
            float[] hsv = { h, 1, 1 };
            palette.add(Color.HSVToColor(hsv));
        }

        for (int h = 0; h < HUE_DEGREES; h += HUE_INCREMENT) {
            float[] hsv = { h, SAT_LUM_ADJUST, 1 };
            palette.add(Color.HSVToColor(hsv));
        }

        for (int h = 0; h < HUE_DEGREES; h += HUE_INCREMENT) {
            float[] hsv = { h, 1, SAT_LUM_ADJUST };
            palette.add(Color.HSVToColor(hsv));
        }

        return palette;
    }

    /**
     * Class that contains a pair of intersections that results from a
     * line/circle intersection calculation. Not guaranteed to be in a
     * particular order.
     * 
     * @author Tim
     * 
     */
    public static class IntersectionPair {
        /**
         * The first intersection.
         */
        private PointF mIntersection1;

        /**
         * The second intersection.
         */
        private PointF mIntersection2;

        /**
         * Constructor.
         * 
         * @param i1
         *            First intersection.
         * @param i2
         *            Second intersection.
         */
        public IntersectionPair(PointF i1, PointF i2) {
            mIntersection1 = i1;
            mIntersection2 = i2;
        }

        /**
         * @return The first intersection.
         */
        public PointF getIntersection1() {
            return mIntersection1;
        }

        /**
         * @return The second intersection.
         */
        public PointF getIntersection2() {
            return mIntersection2;
        }
    }

    /**
     * Computes the intersection between a line segment and a circle, and
     * returns the result as an IntersectionPair.
     * 
     * @param p1
     *            First endpoint of the line segment.
     * @param p2
     *            Second endpoint of the line segment.
     * @param center
     *            Center of the circle.
     * @param radius
     *            Radius of the circle.
     * @return IntersectionPair with the intersections.
     */
    public static IntersectionPair lineCircleIntersection(PointF p1, PointF p2,
            PointF center, float radius) {
        // First, make sure that the line segment is anywhere near the circle.
        if (center.x + radius < Math.min(p1.x, p2.x)
                || center.x - radius > Math.max(p1.x, p2.x)
                || center.y + radius < Math.min(p1.y, p2.y)
                || center.y - radius > Math.max(p1.y, p2.y)) {
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

        // Intersection if the discriminant is non-negative. In this
        // case we move on and do even more complicated stuff to erase part of
        // the line.
        if (discriminant > 0) {
            float signDy = dy < 0 ? -1 : 1;
            // Now, compute the x coordinates of the real intersection with the
            // line, not the line segment. Again, this is from Wolfram.
            float intersect1X = (float) ((det * dy - signDy * dx
                    * Math.sqrt(discriminant)) / dsquared)
                    + center.x;
            float intersect2X = (float) ((det * dy + signDy * dx
                    * Math.sqrt(discriminant)) / dsquared)
                    + center.x;

            float intersect1Y = (float) ((det * dx - Math.abs(dy)
                    * Math.sqrt(discriminant)) / dsquared)
                    + center.y;
            float intersect2Y = (float) ((det * dx + Math.abs(dy)
                    * Math.sqrt(discriminant)) / dsquared)
                    + center.y;

            return new IntersectionPair(new PointF(intersect1X, intersect1Y),
                    new PointF(intersect2X, intersect2Y));
        } else {
            return null;
        }
    }

    /**
     * Find a correct scale factor and decode the bitmap from the given URI.
     * See: http://stackoverflow.com/questions/2507898/how-to-pick-a-image-from-
     * gallery-sd-card-for-my-app-in-android
     * 
     * @param selectedImage
     *            Path to the image.
     * @return Decoded image.
     * @throws FileNotFoundException
     *             If image couldn't be found.
     */
    public static Bitmap loadImageWithMaxBounds(Uri selectedImage,
            int maxWidth, int maxHeight, ContentResolver contentResolver)
            throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                contentResolver.openInputStream(selectedImage), null, o);

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < maxWidth || height_tmp / 2 < maxHeight) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                contentResolver.openInputStream(selectedImage), null, o2);
    }

    /**
     * Utility class - private constructor.
     */
    private Util() {
    }
}
