package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


import android.graphics.Path;

/**
 * Encapsulates a single vector-drawn line.
 * @author Tim
 *
 */
public final class FreehandLine extends Shape implements Serializable {

    /**
     * ID for serialization.
     */
    private static final long serialVersionUID = -4935518208097034463L;

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
    public FreehandLine(final int color, final float newLineStrokeWidth) {
        this.mColor = color;
        this.mWidth = newLineStrokeWidth;
    }

    /**
     * Adds the given point to the line.
     * @param p The point to add.
     */
    @Override
	public void addPoint(final PointF p) {
        points.add(p);
        shouldDraw.add(true);
        boundingRectangle.updateBounds(p);
        invalidatePath();
    }


	/**
	 * Creates a new Path object that draws this shape.
	 * @return The created path;
	 */
	@Override
	protected Path createPath() {
		//Do not try to draw a line with too few points.
		if (points.size() < 2) {
			return null;
		}

		Path path = new Path();
		boolean penDown = false;
		for (int i = 0; i < points.size(); ++i) {
		    if (shouldDraw.get(i).booleanValue()) {
		    	PointF p1 = points.get(i);
		    	if (penDown) {
		    		path.lineTo(p1.x, p1.y);
		    	} else {
		    		path.moveTo(p1.x, p1.y);
		    	}
		    	penDown = true;
		    } else {
		    	penDown = false;
		    }
		}
		return path;
	}

	/**
     * Erases all points in the line that fall in the circle specified by
     * the given center and radius.  This does not delete the points, just
     * marks them as erased.  removeErasedPoints() needs to be called afterward
     * to get the true result of the erase operation.
     * @param center Center of the circle to erase.
     * @param radius Radius of the circle to erase.
     */
    @Override
	public void erase(final PointF center, final float radius) {
        if (boundingRectangle.intersectsWithCircle(center, radius)) {
	        for (int i = 0; i < points.size(); ++i) {
	            if (Util.distance(center, points.get(i)) < radius) {
	                shouldDraw.set(i, false);
	            }
	        }
        }
        invalidatePath();
    }

    /**
     * Returns a list of lines that are created by removing any erased points
     * from this line.  There will often be more than one line returned, as it
     * is a common case to erase the middle of the line.  The current line is
     * set to draw all points again.
     * @return A list of lines that results from removing erased points.
     */
    @Override
	public List<Shape> removeErasedPoints() {
        List<Shape> optimizedLines = new ArrayList<Shape>();
        FreehandLine l = new FreehandLine(mColor, mWidth);
        optimizedLines.add(l);
        for (int i = 0; i < points.size(); ++i) {
            if (this.shouldDraw.get(i).booleanValue()) {
                l.addPoint(points.get(i));
            } else if (l.points.size() > 0) {
                //Do not add a line with only one point in it, those are useless
                if (l.points.size() == 1) {
                    optimizedLines.remove(l);
                }
                l = new FreehandLine(mColor, mWidth);
                optimizedLines.add(l);
            }
            this.shouldDraw.set(i, true);
        }

        // shouldDraw was reset, path is invalid
        invalidatePath();

        return optimizedLines;
    }

    /**
     * @return True if an optimization pass is needed on this line (i.e. if it
     * was just erased).
     */
    @Override
	public boolean needsOptimization() {
    	for (boolean b : shouldDraw) {
    		if (!b) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Checks whether this point falls in the polygon created by closing this
     * path.
     * @param p The point to test
     * @return True if the polygon contains the point.
     */
    @Override
	public boolean contains(PointF p) {
    	// First, check whether the bounding rectangle contains the point so
    	// we can efficiently and quickly get rid of easy cases.
    	if (!this.boundingRectangle.contains(p)) {
    		return false;
    	}

    	// This algorithm adapted from http://alienryderflex.com/polygon/
    	// The algorithm tests whether a horizontal line drawn through the test
    	// point intersects an odd number of polygon sides to the left of the
    	// point.

    	// i and j store consecutive points, so they define a line segment.
    	// Start with the line segment from the last point to the first point.
    	int j = points.size() - 1;
    	boolean oddNodes = false;
    	for (int i = 0; i < points.size(); ++i) {
    		PointF pj = points.get(j);
    		PointF pi = points.get(i);

    		// Check if the test point is in between the y coordinates of the
    		// two points that make up this line segment.  This checks two
    		// conditions: whether the horizontal line has an intersection
    		// (avoids division by 0), and whether the intersection between
    		// the extruded line and horizontal line occurs on the line segment.
    		if (pi.y < p.y && pj.y >= p.y || pj.y < p.y && pi.y >= p.y) {
    			// Check if the horizontal line/line segment intersectino occurs
    			// to the left of the test point.
    			if (pi.x + (p.y - pi.y) / (pj.y - pi.y) * (pj.x - pi.x) < p.x) {
    		        oddNodes = !oddNodes;
    		    }
    		}
    		j = i;
    	}
    	return oddNodes;
    }
}