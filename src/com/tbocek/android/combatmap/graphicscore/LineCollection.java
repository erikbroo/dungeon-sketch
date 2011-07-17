package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.graphics.Canvas;

/**
 * Provides operations over an aggregate collection of lines.
 * Invariant: Lines are sorted by descending stroke width.  This is so that
 * a thick line can be used to paint an area bounded by a thin line.
 * @author Tim Bocek
 */
public final class LineCollection implements Serializable {
	/**
	 * ID for serialization.
	 */
	private static final long serialVersionUID = 3807015512261579274L;
	
	/**
	 * The internal list of lines.
	 */
	private List<Line> lines = new LinkedList<Line>();
	
	/**
	 * Draws all lines on the given canvas.
	 * @param canvas The canvas to draw on.
	 * @param transformer Transformer between screen space and world space.
	 */
	public void drawAllLines(
			final Canvas canvas, final CoordinateTransformer transformer) {
		for (int i = 0; i < lines.size(); ++i) {
			lines.get(i).draw(canvas, transformer);
		}
	}

	/**
	 * Factory method that creates a line, adds it to the list of lines, and
	 * returns the newly created line.
	 * @param newLineColor The new line's color.
	 * @param newLineStrokeWidth The new line's stroke width.
	 * @return The new line.
	 */
	public Line createLine(
			final int newLineColor, final int newLineStrokeWidth) {
		Line l = new Line(newLineColor, newLineStrokeWidth);
		insertLine(l);
		return l;
	}
	
	/**
	 * Inserts a new line into the list of lines, making sure that the lines are
	 * sorted by line width.
	 * @param line The line to add.
	 */
	private void insertLine(final Line line) {
		if (lines.isEmpty()) {
			lines.add(line);
			return;
		}
		
		ListIterator<Line> it = lines.listIterator();
		while (it.hasNext()
				&& lines.get(it.nextIndex()).getStrokeWidth() 
					>= line.getStrokeWidth()) {
			it.next();
		}
		it.add(line);
	}

	/**
	 * Removes all lines.
	 */
	public void clear() {
		lines.clear();
	}
	
	/**
	 * Erases all points on lines centered at a given location.
	 * @param location The point in world space to center the erase on.
	 * @param radius Radius around the point to erase, in world space.
	 */
	public void erase(final PointF location, final float radius) {
		for (int i = 0; i < lines.size(); ++i) {
			lines.get(i).erase(location, radius);
		}
	}
	
	/**
	 * Performs an optimization pass on the lines.  This removes all erased
	 * points (rather than keeping them marked as not drawn), and splits
	 * each line with erased points into individual lines representing the
	 * newly disjoint sections.
	 */
	public void optimize() {
		List<Line> newLines = new LinkedList<Line>();
		for (int i = 0; i < lines.size(); ++i) {
			List<Line> optimizedLines = lines.get(i).removeErasedPoints();
			newLines.addAll(optimizedLines);
		}
		lines.clear();
		lines.addAll(newLines);
	}

	/**
	 * @return True if this collection has no lines in it, False otherwise.
	 */
	public boolean isEmpty() {
		return lines.isEmpty();
	}
	
	/**
	 * @return The bounding rectangle that bounds all lines in the collection.
	 */
	public BoundingRectangle getBoundingRectangle() {
		BoundingRectangle r = new BoundingRectangle();
		for (Line l : lines) {
			r.updateBounds(l.getBoundingRectangle());
		}
		return r;
	}
}
