package com.tbocek.android.combatmap.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region.Op;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;
import com.tbocek.android.combatmap.model.primitives.BoundingRectangle;
import com.tbocek.android.combatmap.model.primitives.Circle;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.FreehandLine;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Rectangle;
import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.model.primitives.StraightLine;
import com.tbocek.android.combatmap.model.primitives.Text;

/**
 * Provides operations over an aggregate collection of lines. Invariant: Lines
 * are sorted by descending stroke width. This is so that a thick line can be
 * used to paint an area bounded by a thin line.
 * 
 * @author Tim Bocek
 */
public final class LineCollection implements UndoRedoTarget {

    /**
     * The internal list of lines.
     */
    private List<Shape> mLines = new LinkedList<Shape>();

    /**
     * Undo/Redo History.
     */
    private CommandHistory mCommandHistory;

    /**
     * Constructor allowing multiple line collections to share one undo/redo
     * history.
     * 
     * @param history
     *            The undo/redo history.
     */
    public LineCollection(CommandHistory history) {
        mCommandHistory = history;
    }

    /**
     * Draws all lines on the given canvas.
     * 
     * @param canvas
     *            The canvas to draw on.
     */
    public void drawAllLines(final Canvas canvas) {
        for (int i = 0; i < mLines.size(); ++i) {
            mLines.get(i).applyDrawOffsetToCanvas(canvas);
            mLines.get(i).draw(canvas);
            mLines.get(i).revertDrawOffsetFromCanvas(canvas);
        }
    }

    /**
     * Draws all lines on the given canvas that should be drawn below the grid.
     * 
     * @param canvas
     *            The canvas to draw on.
     */
    public void drawAllLinesBelowGrid(final Canvas canvas) {
        for (int i = 0; i < mLines.size(); ++i) {
            if (mLines.get(i).shouldDrawBelowGrid()) {
                mLines.get(i).applyDrawOffsetToCanvas(canvas);
                mLines.get(i).draw(canvas);
                mLines.get(i).revertDrawOffsetFromCanvas(canvas);
            }
        }
    }

    /**
     * Draws all lines on the given canvas that should be drawn above the grid.
     * 
     * @param canvas
     *            The canvas to draw on.
     */
    public void drawAllLinesAboveGrid(final Canvas canvas) {
        for (int i = 0; i < mLines.size(); ++i) {
            if (!mLines.get(i).shouldDrawBelowGrid()) {
                mLines.get(i).applyDrawOffsetToCanvas(canvas);
                mLines.get(i).draw(canvas);
                mLines.get(i).revertDrawOffsetFromCanvas(canvas);
            }
        }
    }

    /**
     * Draws all lines on the given canvas.
     * 
     * @param canvas
     *            The canvas to draw on.
     */
    public void drawFogOfWar(final Canvas canvas) {
        for (int i = 0; i < mLines.size(); ++i) {
            mLines.get(i).drawFogOfWar(canvas);
        }
    }

    /**
     * Draws all lines on the given canvas.
     * 
     * @param canvas
     *            The canvas to draw on.
     */
    public void clipFogOfWar(final Canvas canvas) {
        Rect r = canvas.getClipBounds();

        // Remove the current clip.
        canvas.clipRect(r, Op.DIFFERENCE);

        // Union together the regions that are supposed to draw.
        for (int i = 0; i < mLines.size(); ++i) {
            mLines.get(i).clipFogOfWar(canvas);
        }

        canvas.clipRect(r, Op.INTERSECT);
    }

    /**
     * Factory method that creates a freehand line, adds it to the list of
     * lines, and returns the newly created line.
     * 
     * @param newLineColor
     *            The new line's color.
     * @param newLineStrokeWidth
     *            The new line's stroke width.
     * @return The new line.
     */
    public Shape createFreehandLine(final int newLineColor,
            final float newLineStrokeWidth) {
        FreehandLine l = new FreehandLine(newLineColor, newLineStrokeWidth);
        Command c = new Command(this);
        c.addCreatedShape(l);
        mCommandHistory.execute(c);
        return l;
    }

    /**
     * Factory method that creates a straight line, adds it to the list of
     * lines, and returns the newly created line.
     * 
     * @param newLineColor
     *            The new line's color.
     * @param newLineStrokeWidth
     *            The new line's stroke width.
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
     * Factory method that creates a circle, adds it to the list of lines, and
     * returns the newly created line.
     * 
     * @param newLineColor
     *            The new line's color.
     * @param newLineStrokeWidth
     *            The new line's stroke width.
     * @return The new line.
     */
    public Shape createCircle(int newLineColor, float newLineStrokeWidth) {
        Circle l = new Circle(newLineColor, newLineStrokeWidth);
        Command c = new Command(this);
        c.addCreatedShape(l);
        mCommandHistory.execute(c);
        return l;
    }

    /**
     * Factory method that creates a rectangle, adds it to the list of lines,
     * and returns the newly created line.
     * 
     * @param newLineColor
     *            The new line's color.
     * @param newLineStrokeWidth
     *            The new line's stroke width.
     * @return The new line.
     */
    public Shape createRectangle(int newLineColor, float newLineStrokeWidth) {
        Rectangle l = new Rectangle(newLineColor, newLineStrokeWidth);
        Command c = new Command(this);
        c.addCreatedShape(l);
        mCommandHistory.execute(c);
        return l;
    }

    /**
     * Creates a new text object with the given parameters in this line
     * collection.
     * 
     * @param text
     *            Text that the object contains.
     * @param size
     *            Font size.
     * @param color
     *            Font color.
     * @param strokeWidth
     *            Stroke width (currently unused).
     * @param location
     *            Location.
     * @param transform
     *            World to screen space transformer.
     * @return The created text object.
     */
    public Shape createText(String text, float size, int color,
            float strokeWidth, PointF location, CoordinateTransformer transform) {
        Text t = new Text(text, size, color, strokeWidth, location, transform);
        Command c = new Command(this);
        c.addCreatedShape(t);
        mCommandHistory.execute(c);
        return t;
    }

    /**
     * Modifies the given text object's contents and font.
     * 
     * @param editedTextObject
     *            Text object to modify.
     * @param text
     *            The new text.
     * @param size
     *            The new font size.
     * @param transformer
     *            World to screen space transformer.
     */
    public void editText(Text editedTextObject, String text, float size,
            CoordinateTransformer transformer) {
        Text newText = new Text(text, size, editedTextObject.getColor(),
                editedTextObject.getWidth(), editedTextObject.getLocation(),
                transformer);
        Command c = new Command(this);
        c.addCreatedShape(newText);
        c.addDeletedShape(editedTextObject);
        mCommandHistory.execute(c);
    }

    /**
     * Inserts a new line into the list of lines, making sure that the lines are
     * sorted by line width.
     * 
     * @param line
     *            The line to add.
     */
    private void insertLine(final Shape line) {
        if (mLines.isEmpty()) {
            mLines.add(line);
            return;
        }

        ListIterator<Shape> it = mLines.listIterator();
        while (it.hasNext()
                && mLines.get(it.nextIndex()).getStrokeWidth() >= line
                        .getStrokeWidth()) {
            it.next();
        }
        it.add(line);
    }

    /**
     * Finds and returns the shape under the given point, potentially with the
     * given type. If there are multiple candidates, returns one arbitrarily.
     * 
     * @param under
     *            Point that should lie in the found shape.
     * @param requestedClass
     *            Desired class (subclass of Shape) to find, or null to find
     *            anything.
     * @return A shape that meets the criteria.
     */
    public Shape findShape(final PointF under, final Class<?> requestedClass) {
        for (Shape l : mLines) {
            if ((requestedClass == null || l.getClass() == requestedClass)
                    && l.contains(under)) {
                return l;
            }
        }
        return null;
    }

    /**
     * Finds and returns the shape under the given point. If there are multiple
     * candidates, returns one arbitrarily.
     * 
     * @param under
     *            Point that should lie in the found shape.
     * @return A shape that meets the criteria.
     */
    public Shape findShape(PointF under) {
        return findShape(under, null);
    }

    /**
     * Deletes the given shape.
     * 
     * @param l
     *            The shape to delete.
     */
    public void deleteShape(Shape l) {
        if (mLines.contains(l)) {
            Command c = new Command(this);
            c.addDeletedShape(l);
            mCommandHistory.execute(c);
        }
    }

    /**
     * Removes all lines.
     */
    public void clear() {
        mLines.clear();
    }

    /**
     * Erases all points on lines centered at a given location.
     * 
     * @param location
     *            The point in world space to center the erase on.
     * @param radius
     *            Radius around the point to erase, in world space.
     */
    public void erase(final PointF location, final float radius) {
        for (int i = 0; i < mLines.size(); ++i) {
            mLines.get(i).erase(location, radius);
        }
    }

    /**
     * Performs an optimization pass on the lines. This removes all erased
     * points (rather than keeping them marked as not drawn), and splits each
     * line with erased points into individual lines representing the newly
     * disjoint sections.
     */
    public void optimize() {
        Command c = new Command(this);
        for (int i = 0; i < mLines.size(); ++i) {
            if (!mLines.get(i).isValid()) {
                c.addDeletedShape(mLines.get(i));
            } else if (mLines.get(i).needsOptimization()) {
                List<Shape> optimizedLines = mLines.get(i).removeErasedPoints();
                c.addDeletedShape(mLines.get(i));
                c.addCreatedShapes(optimizedLines);
            } else if (mLines.get(i).hasOffset()) {
                c.addDeletedShape(mLines.get(i));
                c.addCreatedShape(mLines.get(i).commitDrawOffset());
            }
        }
        mCommandHistory.execute(c);
    }

    /**
     * @return True if this collection has no lines in it, False otherwise.
     */
    public boolean isEmpty() {
        return mLines.isEmpty();
    }

    /**
     * Saves this line collection to the given stream.
     * 
     * @param s
     *            Stream to save to.
     * @throws IOException
     *             On serialization error.
     */
    public void serialize(MapDataSerializer s) throws IOException {
        s.startArray();
        for (Shape shape : this.mLines) {
            if (shape.isValid()) {
                shape.serialize(s);
            }
        }
        s.endArray();
    }

    /**
     * Populates this line collection by reading from the given stream.
     * 
     * @param s
     *            Stream to load from.
     * @throws IOException
     *             On deserialization error.
     */
    public void deserialize(MapDataDeserializer s) throws IOException {
        int arrayLevel = s.expectArrayStart();
        while (s.hasMoreArrayItems(arrayLevel)) {
            this.mLines.add(Shape.deserialize(s));
        }
        s.expectArrayEnd();
    }

    /**
     * @return The bounding rectangle that bounds all lines in the collection.
     */
    public BoundingRectangle getBoundingRectangle() {
        BoundingRectangle r = new BoundingRectangle();
        for (Shape l : mLines) {
            r.updateBounds(l.getBoundingRectangle());
        }
        return r;
    }

    /**
     * This class represents a command that adds and deletes lines.
     * 
     * @author Tim Bocek
     * 
     */
    private static class Command implements CommandHistory.Command {
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
         * 
         * @param lineCollection
         *            The LineCollection that this command modifies.
         */
        public Command(final LineCollection lineCollection) {
            mLineCollection = lineCollection;
        }

        /**
         * Executes the command on the LineCollection that this command mutates.
         */
        public void execute() {
            List<Shape> newLines = new LinkedList<Shape>();
            for (Shape l : mLineCollection.mLines) {
                if (!mDeleted.contains(l)) {
                    newLines.add(l);
                }
            }
            mLineCollection.mLines = newLines;

            for (Shape l : mCreated) {
                mLineCollection.insertLine(l);
            }
        }

        /**
         * Undoes the command on the LineCollection that this command mutates.
         */
        public void undo() {
            List<Shape> newLines = new LinkedList<Shape>();
            for (Shape l : mLineCollection.mLines) {
                if (!mCreated.contains(l)) {
                    newLines.add(l);
                }
            }
            mLineCollection.mLines = newLines;

            for (Shape l : mDeleted) {
                mLineCollection.insertLine(l);
            }
        }

        /**
         * Adds a line to the list of lines created by this command.
         * 
         * @param l
         *            The line to add.
         */
        public void addCreatedShape(final Shape l) {
            mCreated.add(l);
        }

        /**
         * Adds several lines to the list of lines created by this command.
         * 
         * @param lc
         *            The lines to add.
         */
        public void addCreatedShapes(final Collection<Shape> lc) {
            mCreated.addAll(lc);
        }

        /**
         * Adds a line to the list of lines removed by this command.
         * 
         * @param l
         *            The line to remove.
         */
        public void addDeletedShape(final Shape l) {
            mDeleted.add(l);
        }

        /**
         * @return True if the command is a no-op, false if it modifies lines.
         *         noop.
         */
        public boolean isNoop() {
            return mCreated.isEmpty() && mDeleted.isEmpty();
        }
    }

    /**
     * Undoes an operation, if there is one to undo.
     */
    public void undo() {
        mCommandHistory.undo();
    }

    /**
     * Redoes an operation, if there is one to redo.
     */
    public void redo() {
        mCommandHistory.redo();
    }

    @Override
    public boolean canUndo() {
        return mCommandHistory.canUndo();
    }

    @Override
    public boolean canRedo() {
        return mCommandHistory.canRedo();
    }

}
