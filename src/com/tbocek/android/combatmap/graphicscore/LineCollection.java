package com.tbocek.android.combatmap.graphicscore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

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
    private List<Shape> lines = new LinkedList<Shape>();

    /**
     * Undo/Redo History.
     */
    private CommandHistory mCommandHistory;

    /**
     * Constructor allowing multiple line collections to share one undo/redo
     * history.
     * @param history The undo/redo history.
     */
    public LineCollection(CommandHistory history) {
    	mCommandHistory = history;
    }

	/**
     * Draws all lines on the given canvas.
     * @param canvas The canvas to draw on.
     */
    public void drawAllLines(final Canvas canvas) {
        for (int i = 0; i < lines.size(); ++i) {
        	lines.get(i).applyDrawOffsetToCanvas(canvas);
            lines.get(i).draw(canvas);
            lines.get(i).revertDrawOffsetFromCanvas(canvas);
        }
    }

	/**
     * Draws all lines on the given canvas that should be drawn below the grid.
     * @param canvas The canvas to draw on.
     */
    public void drawAllLinesBelowGrid(final Canvas canvas) {
        for (int i = 0; i < lines.size(); ++i) {
        	if (lines.get(i).shouldDrawBelowGrid()) {
        		lines.get(i).applyDrawOffsetToCanvas(canvas);
        		lines.get(i).draw(canvas);
        		lines.get(i).revertDrawOffsetFromCanvas(canvas);
        	}
        }
    }


	/**
     * Draws all lines on the given canvas that should be drawn above the grid.
     * @param canvas The canvas to draw on.
     */
    public void drawAllLinesAboveGrid(final Canvas canvas) {
        for (int i = 0; i < lines.size(); ++i) {
        	if (!lines.get(i).shouldDrawBelowGrid()) {
        		lines.get(i).applyDrawOffsetToCanvas(canvas);
        		lines.get(i).draw(canvas);
        		lines.get(i).revertDrawOffsetFromCanvas(canvas);
        	}
        }
    }

    /**
     * Draws all lines on the given canvas.
     * @param canvas The canvas to draw on.
     */
    public void drawFogOfWar(final Canvas canvas) {
        for (int i = 0; i < lines.size(); ++i) {
            lines.get(i).drawFogOfWar(canvas);
        }
    }

    /**
     * Draws all lines on the given canvas.
     * @param canvas The canvas to draw on.
     */
    public void clipFogOfWar(final Canvas canvas) {
        for (int i = 0; i < lines.size(); ++i) {
            lines.get(i).clipFogOfWar(canvas);
        }
    }

    /**
     * Factory method that creates a freehand line, adds it to the list of
     * lines, and returns the newly created line.
     * @param newLineColor The new line's color.
     * @param newLineStrokeWidth The new line's stroke width.
     * @return The new line.
     */
    public Shape createFreehandLine(
            final int newLineColor, final float newLineStrokeWidth) {
        FreehandLine l = new FreehandLine(newLineColor, newLineStrokeWidth);
        Command c = new Command(this);
        c.addCreatedShape(l);
        mCommandHistory.execute(c);
        return l;
    }

    /**
     * Factory method that creates a straight line, adds it to the list of
     * lines, and returns the newly created line.
     * @param newLineColor The new line's color.
     * @param newLineStrokeWidth The new line's stroke width.
     * @return The new line.
     */
	public Shape createStraightLine(int newLineColor, float newLineStrokeWidth) {
        StraightLine l = new StraightLine(newLineColor, newLineStrokeWidth);
        Command c = new Command(this);
        c.addCreatedShape(l);
        mCommandHistory.execute(c);
        return l;
	}

    /**
     * Factory method that creates a circle, adds it to the list of
     * lines, and returns the newly created line.
     * @param newLineColor The new line's color.
     * @param newLineStrokeWidth The new line's stroke width.
     * @return The new line.
     */
	public Shape createCircle(int newLineColor, float newLineStrokeWidth) {
        Circle l = new Circle(newLineColor, newLineStrokeWidth);
        Command c = new Command(this);
        c.addCreatedShape(l);
        mCommandHistory.execute(c);
        return l;
	}
	

	public Shape createText(String text, float size, int color, float strokeWidth, PointF location, CoordinateTransformer transform) {
		Text t = new Text(text, size, color, strokeWidth, location, transform);
		Command c = new Command(this);
		c.addCreatedShape(t);
		mCommandHistory.execute(c);
		return t;
	}
	

	public void editText(Text editedTextObject, String text, float size, CoordinateTransformer transformer) {
		Text newText = new Text(text, size, editedTextObject.mColor, editedTextObject.mWidth, editedTextObject.location, transformer);
		Command c = new Command(this);
		c.addCreatedShape(newText);
		c.addDeletedShape(editedTextObject);
		mCommandHistory.execute(c);
	}

	
    /**
     * Inserts a new line into the list of lines, making sure that the lines are
     * sorted by line width.
     * @param line The line to add.
     */
    private void insertLine(final Shape line) {
        if (lines.isEmpty()) {
            lines.add(line);
            return;
        }

        ListIterator<Shape> it = lines.listIterator();
        while (it.hasNext()
                && lines.get(it.nextIndex()).getStrokeWidth()
                    >= line.getStrokeWidth()) {
            it.next();
        }
        it.add(line);
    }

    public Shape findShape(final PointF under, final Class<?> requestedClass) {
    	for (Shape l : lines) {
        	if ((requestedClass == null || l.getClass() == requestedClass) 
        			&& l.contains(under)) {
        		return l;
        	}
        }
    	return null;
    }
    

	public Shape findShape(PointF under) {
		return findShape(under, null);
	}

	/**
     * Deletes all regions under the tapped point.
     * @param tappedPoint The point that was tapped, in world space.
     */
    public void deleteShape(Shape l) {
    	if (lines.contains(l)) {
    		Command c = new Command(this);
    		c.addDeletedShape(l);
    		mCommandHistory.execute(c);
    	}
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
    	Command c = new Command(this);
        for (int i = 0; i < lines.size(); ++i) {
        	if (lines.get(i).needsOptimization()) {
	            List<Shape> optimizedLines = lines.get(i).removeErasedPoints();
	            c.addDeletedShape(lines.get(i));
	            c.addCreatedShapes(optimizedLines);
        	} else if (lines.get(i).hasOffset()) {
        		c.addDeletedShape(lines.get(i));
        		c.addCreatedShape(lines.get(i).commitDrawOffset());
        	}
        }
        mCommandHistory.execute(c);
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
        for (Shape l : lines) {
            r.updateBounds(l.getBoundingRectangle());
        }
        return r;
    }

    /**
     * This class represents a command that adds and deletes lines.
     * @author Tim Bocek
     *
     */
    private class Command {
    	/**
    	 * Lines created in this operation.
    	 */
    	private Collection<Shape> mCreated = new ArrayList<Shape>();

    	/**
    	 * Lines deleted in this operation.
    	 */
    	private Collection<Shape> mDeleted = new ArrayList<Shape>();

    	/**
    	 * Collection of lines to modify.
    	 */
    	private LineCollection mLineCollection;

    	/**
    	 * Constructor.
    	 * @param lineCollection The LineCollection that this command modifies.
    	 */
    	public Command(final LineCollection lineCollection) {
    		mLineCollection = lineCollection;
    	}

    	/**
    	 * Executes the command on the LineCollection that this command mutates.
    	 */
    	public void execute() {
    		List<Shape> newLines = new LinkedList<Shape>();
    		for (Shape l : mLineCollection.lines) {
    			if (!mDeleted.contains(l)) {
    				newLines.add(l);
    			}
    		}
    		mLineCollection.lines = newLines;

    		for (Shape l : mCreated) {
    			mLineCollection.insertLine(l);
    		}
    	}

    	/**
    	 * Undoes the command on the LineCollection that this command mutates.
    	 */
    	public void undo() {
    		List<Shape> newLines = new LinkedList<Shape>();
    		for (Shape l : mLineCollection.lines) {
    			Shape DEBUG_LINE = l;
    			if (!mCreated.contains(l)) {
    				newLines.add(l);
    			}
    		}
    		mLineCollection.lines = newLines;

    		for (Shape l : mDeleted) {
    			mLineCollection.insertLine(l);
    		}
    	}

    	/**
    	 * Adds a line to the list of lines created by this command.
    	 * @param l The line to add.
    	 */
    	public void addCreatedShape(final Shape l) {
    		mCreated.add(l);
    	}

    	/**
    	 * Adds several lines to the list of lines created by this command.
    	 * @param lc The lines to add.
    	 */
    	public void addCreatedShapes(final Collection<Shape> lc) {
    		mCreated.addAll(lc);
    	}

    	/**
    	 * Adds a line to the list of lines removed by this command.
    	 * @param l The line to remove.
    	 */
    	public void addDeletedShape(final Shape l) {
    		mDeleted.add(l);
    	}

    	/**
    	 * @return True if the command is a no-op, false if it modifies lines.
    	 * noop.
    	 */
    	public boolean isNoop() {
    		return mCreated.isEmpty() && mDeleted.isEmpty();
    	}
    }

    public static class CommandHistory implements Serializable {
		/**
		 * Operations on this line collection that are available to undo.
		 */
		private transient Stack<Command> toUndo = new Stack<Command>();
		/**
		 * Operations on this line collection that are available to redo.
		 */
		private transient Stack<Command> toRedo = new Stack<Command>();

	    /**
	     * Undo the last line operation.
	     */
	    public void undo() {
	    	if (canUndo()) {
		    	Command c = toUndo.pop();
		    	c.undo();
		    	toRedo.push(c);
	    	}
	    }

	    /**
	     * Redo the last line operation.
	     */
	    public void redo() {
	    	if (canRedo()) {
		    	Command c = toRedo.pop();
		    	c.execute();
		    	toUndo.push(c);
	    	}
	    }

	    /**
	     * @return True if the undo operation can be performed, false otherwise.
	     */
	    public boolean canUndo() {
	    	return !toUndo.isEmpty();
	    }

	    /**
	     * @return True if the redo operation can be performed, false otherwise.
	     */
	    public boolean canRedo() {
	    	return !toRedo.isEmpty();
	    }

	    /**
	     * Executes the given command.  This should not be called on commands to
	     * redo.
	     * @param command The command to execute.
	     */
	    private void execute(final Command command) {
	    	if (!command.isNoop()) {
		    	command.execute();
		    	toUndo.add(command);
		    	toRedo.clear();
	    	}
	    }

	    /**
	     * Deserializes the object.  This uses the standard deserialization but
	     * must also create transient objects that manage undo and redo.
	     * @param inputStream Stream to deserialize from.
	     * @throws IOException On read error.
	     * @throws ClassNotFoundException On deserialization error.
	     */
	    private void readObject(final ObjectInputStream inputStream)
	    		throws IOException, ClassNotFoundException {
	    	inputStream.defaultReadObject();
	    	toUndo = new Stack<Command>();
	    	toRedo = new Stack<Command>();
	    }
	}

	public void undo() {
		mCommandHistory.undo();
	}

	public void redo() {
		mCommandHistory.redo();
	}


}
