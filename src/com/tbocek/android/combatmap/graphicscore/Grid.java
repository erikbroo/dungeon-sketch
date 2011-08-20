package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;

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
    	g.colorScheme = GridColorScheme.fromNamedScheme(colorScheme);
        g.mGridToWorldTransformer = transformer;
        return g;
    }

    /**
     * The color scheme to use when drawing this grid.
     */
    private GridColorScheme colorScheme = GridColorScheme.GRAPH_PAPER;

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
    protected abstract void drawGrid(
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
    	return this.colorScheme.getLineColor();
    }

    /**
     * @return The color to use when drawing the background.
     * @return
     */
    protected final int getBackgroundColor() {
    	return this.colorScheme.getBackgroundColor();
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

}