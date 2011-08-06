package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Encapsulates a single vector-drawn line.
 * @author Tim
 *
 */
public final class Line implements Serializable {

    /**
     * ID for serialization.
     */
    private static final long serialVersionUID = -4935518208097034463L;

    /**
     * The paint object that will be used to draw this line.
     */
    private transient Paint paint;

    /**
     * The color to draw this line with.
     */
    private int mColor = Color.BLACK;

    /**
     * The stroke width to draw this line with.
     */
    private float mWidth = 2;

    /**
     * Cached rectangle that bounds all the points in this line.
     * This could be computed on demand, but it is easy enough to update
     * every time a point is added.
     */
    private BoundingRectangle boundingRectangle = new BoundingRectangle();

    /**
     * The points that comprise this line.
     */
    private List<PointF> points = new ArrayList<PointF>();

    /**
     * Whether each point in the line should be drawn.  This allows us to
     * temporarily suppress drawing the points when the line is being erased.
     * However, it's only a temporary fix; the line should later be optimized
     * so that points that shouldn't draw get removed instead.
     */
    private List<Boolean> shouldDraw = new ArrayList<Boolean>();

    /**
     * Constructor.
     * @param color Line color.
     * @param newLineStrokeWidth Line stroke width.
     */
    public Line(final int color, final float newLineStrokeWidth) {
        this.mColor = color;
        this.mWidth = newLineStrokeWidth;
    }

    /**
     * Adds the given point to the line.
     * @param p The point to add.
     */
    public void addPoint(final PointF p) {
        points.add(p);
        shouldDraw.add(true);
        boundingRectangle.updateBounds(p);
    }

    /**
     * Draws the line on the given canvas.
     * @param c Canvas to draw on.
     * @param transformer World space to screen space transformer.
     */
    public void draw(final Canvas c) {
        //Do not try to draw a line with too few points.
        if (points.size() < 2) {
        	return;
        }

        ensurePaintCreated();
        for (int i = 0; i < points.size() - 1; ++i) {
            if (shouldDraw.get(i).booleanValue()
            		&& shouldDraw.get(i + 1).booleanValue()) {
                PointF p1 = points.get(i);
                PointF p2 = points.get(i + 1);
                c.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }
    }

	/**
	 * If there is no Paint object cached for this line, create one and set
	 * the appropriate color and stroke width.
	 */
	private void ensurePaintCreated() {
		if (paint == null) {
	        paint = new Paint();
	        paint.setColor(mColor);
	        paint.setStrokeWidth(mWidth);
        }
	}

    /**
     * Erases all points in the line that fall in the circle specified by
     * the given center and radius.  This does not delete the points, just
     * marks them as erased.  removeErasedPoints() needs to be called afterward
     * to get the true result of the erase operation.
     * @param center Center of the circle to erase.
     * @param radius Radius of the circle to erase.
     */
    public void erase(final PointF center, final float radius) {
        if (boundingRectangle.intersectsWithCircle(center, radius)) {
            for (int i = 0; i < points.size(); ++i) {
                if (Util.distance(center, points.get(i)) < radius) {
                    shouldDraw.set(i, false);
                }
            }
        }
    }

    /**
     * Returns a list of lines that are created by removing any erased points
     * from this line.  There will often be more than one line returned, as it
     * is a common case to erase the middle of the line.  The current line is
     * set to draw all points again.
     * @return A list of lines that results from removing erased points.
     */
    public List<Line> removeErasedPoints() {
        List<Line> optimizedLines = new ArrayList<Line>();
        Line l = new Line(mColor, mWidth);
        optimizedLines.add(l);
        for (int i = 0; i < points.size(); ++i) {
            if (this.shouldDraw.get(i).booleanValue()) {
                l.addPoint(points.get(i));
            } else if (l.points.size() > 0) {
                //Do not add a line with only one point in it, those are useless
                if (l.points.size() == 1) {
                    optimizedLines.remove(l);
                }
                l = new Line(mColor, mWidth);
                optimizedLines.add(l);
            }
            this.shouldDraw.set(i, true);
        }

        return optimizedLines;
    }

    /**
     * @return True if an optimization pass is needed on this line (i.e. if it
     * was just erased).
     */
    public boolean needsOptimization() {
    	for (boolean b : shouldDraw) {
    		if (!b) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Gets the smallest rectangle needed to fully enclose the line.
     * @return The bounding rectangle.
     */
    public BoundingRectangle getBoundingRectangle() {
        return boundingRectangle;
    }

    /**
     * @return This line's stroke width.
     */
    public float getStrokeWidth() {
        return this.mWidth;
    }
}
