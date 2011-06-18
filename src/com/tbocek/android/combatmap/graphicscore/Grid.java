package com.tbocek.android.combatmap.graphicscore;

import java.io.Serializable;

import android.graphics.Canvas;

public abstract class Grid implements Serializable {
	private static final long serialVersionUID = -6584074665656742604L;

	public static Grid createGrid(String gridStyle, String colorScheme) {
		Grid g = gridStyle.equals("hex") ? new HexGrid() : new RectangularGrid();
		g.colorScheme = GridColorScheme.fromNamedScheme(colorScheme);
		return g;
	}
	
	public static Grid createGrid(String gridStyle, String colorScheme, CoordinateTransformer transformer) {
		Grid g = createGrid(gridStyle, colorScheme);
		g.mGridToWorldTransformer = transformer;
		return g;
	}
	
	public GridColorScheme colorScheme = GridColorScheme.GRAPH_PAPER;

	public abstract PointF getNearestSnapPoint(PointF currentLocation, float tokenDiameter);

	protected abstract void drawGrid(Canvas canvas, CoordinateTransformer worldToScreenTransformer);

	private CoordinateTransformer mGridToWorldTransformer = new CoordinateTransformer(0,0,1);

	public void draw(Canvas canvas, CoordinateTransformer transformer) {
		drawBackground(canvas);
		drawGrid(canvas, transformer);
	}

	private void drawBackground(Canvas canvas) {
		canvas.drawColor(colorScheme.getBackgroundColor());
	}

	public CoordinateTransformer gridSpaceToScreenSpaceTransformer(CoordinateTransformer worldToScreen) {
		return mGridToWorldTransformer.compose(worldToScreen);
	}

	public CoordinateTransformer gridSpaceToWorldSpaceTransformer() {
		return mGridToWorldTransformer;
	}

}