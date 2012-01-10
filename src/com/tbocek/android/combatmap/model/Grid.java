package com.tbocek.android.combatmap.model;

import java.io.IOException;
import java.io.Serializable;

import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;

import android.graphics.Canvas;

/**
 * Abstract class for the grid lines that are drawn on the main combat canvas.
 * @author Tim Bocek
 *
 */
public abstract class Grid implements Serializable {
	/**
	 * ID for serialization.
	 */
    private static final long serialVersionUID = -6584074665656742604L;


    /**
     * Factory method that creates a grid with the given parameters.
     * @param gridStyle The style of the grid, either "hex" or "rectangular".
     * @param colorScheme The color scheme of the grid.  Valid color schemes
     * 		are defined in GridColorScheme.
     * @param transformer A grid space to world space transformation to use
     *		in this grid.
     * @return The created grid.
     */
    public static Grid createGrid(
    		final String gridStyle, final String colorScheme,
    		final CoordinateTransformer transformer) {
        Grid g = gridStyle.equals("hex")
    		? new HexGrid()
    		: new RectangularGrid();
    	g.mColorScheme = GridColorScheme.fromNamedScheme(colorScheme);
        g.mGridToWorldTransformer = transformer;
        g.mType = gridStyle;
        return g;
    }
    
    /**
     * Factory method that creates a grid with the given parameters.
     * @param gridStyle The style of the grid, either "hex" or "rectangular".
     * @param colorScheme The GridColorScheme object to use.
     * @param transformer A grid space to world space transformation to use
     *		in this grid.
     * @return The created grid.
     */
    public static Grid createGrid(
    		final String gridStyle, final GridColorScheme colorScheme,
    		final CoordinateTransformer transformer) {
        Grid g = gridStyle.equals("hex")
    		? new HexGrid()
    		: new RectangularGrid();
    	g.mColorScheme = colorScheme;
        g.mGridToWorldTransformer = transformer;
        g.mType = gridStyle;
        return g;
    }

    /**
     * The color scheme to use when drawing this grid.
     */
    private GridColorScheme mColorScheme = GridColorScheme.GRAPH_PAPER;
    
    /**
     * Saved string that lead to this style of grid, for serialization.
     */
    private String mType;

    /**
     * Given a point, returns a the point nearest to that point that will draw
     * a circle of the given diameter snapped to the grid.
     * @param currentLocation The candidate point in grid space.
     * @param tokenDiameter Diameter of the token that will be drawn.
     * @return A point that is snapped to the grid.
     */
    public abstract PointF getNearestSnapPoint(
    		final PointF currentLocation, final float tokenDiameter);

    /**
     * Draws the grid lines.
     * @param canvas Canvas to draw on.
     * @param worldToScreenTransformer Transformation from world space to
     * 		screen space.
     */
    public abstract void drawGrid(
    		final Canvas canvas,
    		final CoordinateTransformer worldToScreenTransformer);

    /**
     * The transformation from grid space to world space.  We track this
     * seperately as a property of the grid so that the grid can easily be
     * resized to fit a drawing.
     */
    private CoordinateTransformer mGridToWorldTransformer =
    	new CoordinateTransformer(0, 0, 1);

    /**
     * @return The color to use when drawing grid lines.
     */
    protected final int getLineColor() {
    	return this.mColorScheme.getLineColor();
    }

    /**
     * @return The color to use when drawing the background.
     * @return
     */
    protected final int getBackgroundColor() {
    	return this.mColorScheme.getBackgroundColor();
    }

    /**
     * @return Whether the grid has a dark background.
     */
    public final boolean isDark() {
    	return this.mColorScheme.isDark();
    }

    /**
     * Draws the grid on the given canvas.
     * @param canvas The canvas to draw on.
     * @param transformer World space to screen space transformer (not grid to
     * 		screen, since grid to world is defined in this class).
     */
    public final void draw(
    		final Canvas canvas, final CoordinateTransformer transformer) {
        drawBackground(canvas);
        drawGrid(canvas, transformer);
    }

    /**
     * Fills the canvas with the background color.
     * @param canvas The canvas to draw on.
     */
    public final void drawBackground(final Canvas canvas) {
        canvas.drawColor(getBackgroundColor());
    }

    /**
     * Gets a transformation between grid space and screen space, by composing
     * the known grid --> world transformation with the given world --> screen
     * transformation.
     * @param worldToScreen Transformation from world space to screen space.
     * @return The grid space to screen space transformation.
     */
    public final CoordinateTransformer gridSpaceToScreenSpaceTransformer(
    		final CoordinateTransformer worldToScreen) {
        return mGridToWorldTransformer.compose(worldToScreen);
    }

    /**
     * Returns the stored transformation from grid space to world space.
     * @return The grid space to world space transformation.
     */
    public final CoordinateTransformer gridSpaceToWorldSpaceTransformer() {
        return mGridToWorldTransformer;
    }
    
    /**
     * Writes this Grid object to the given serialization stream.
     * @param s Stream to write to.
     * @throws IOException On serialization error.
     */
    public void serialize(MapDataSerializer s) throws IOException {
    	s.startObject();
    	s.serializeString(mType);
    	this.mColorScheme.serialize(s);
    	this.mGridToWorldTransformer.serialize(s);
    	s.endObject();
    }
    
    /**
     * Loads and returns a Grid object from the given deserialization stream.
     * @param s Stream to read from.
     * @return The loaded token object.
     * @throws IOException On deserialization error.
     */
    public static Grid deserialize(MapDataDeserializer s) throws IOException {
    	s.expectObjectStart();
    	String type = s.readString();
    	GridColorScheme colorScheme = GridColorScheme.deserialize(s);
    	CoordinateTransformer transform = CoordinateTransformer.deserialize(s);
    	s.expectObjectEnd();
    	return createGrid(type, colorScheme, transform);
    }
}