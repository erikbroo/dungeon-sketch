package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Represents a grid of squares that will draw as virtual "graph paper".
 * @author Tim Bocek
 */
public class RectangularGrid extends Grid {
	
	/**
	 * Every MAJOR_GRID_LINE_FREQUENCYth line will be a major grid line.
	 */
	private static final int MAJOR_GRID_LINE_FREQUENCY = 5;
	
	/**
	 * Maximum size of squares formed by major grid lines in pixels.
	 */
	private static final int MAJOR_GRID_LINE_SIZE_LIMIT = 4;
	
	/**
	 * Maximum size of squares formed by minor grid lines in pixels.
	 */
	private static final int MINOR_GRID_LINE_SIZE_LIMIT = 8;
	
	/**
	 * Width to draw major grid lines with.
	 */
	private static final float MAJOR_GRID_LINE_WIDTH = 3;
	
	/**
	 * Width to draw minor grid lines with.
	 */
	private static final float MINOR_GRID_LINE_WIDTH = 3;
	
	@Override
	protected final void drawGrid(
			final Canvas canvas, 
			final CoordinateTransformer worldToScreenTransformer) {
		Paint paint = new Paint();
		paint.setColor(colorScheme.getLineColor());
		
		CoordinateTransformer transformer =
			gridSpaceToScreenSpaceTransformer(worldToScreenTransformer);
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		float squareSize = transformer.worldSpaceToScreenSpace(1.0f);
		float numSquaresHorizontal = (float) width / squareSize;
		float numSquaresVertical =
			(numSquaresHorizontal * ((float) height) / ((float) width));
		
		boolean shouldDrawMinorLines =
			squareSize >= MINOR_GRID_LINE_SIZE_LIMIT;
		boolean shouldDrawMajorLines =
			squareSize >= MAJOR_GRID_LINE_SIZE_LIMIT;
		boolean shouldDrawCurrentLine = true;
		
		PointF origin = transformer.getOrigin();
		
		float offsetX = origin.x % squareSize;
		float offsetY = origin.y % squareSize;
		
		int thickLineStartX =
			(int) ((origin.x % (squareSize * MAJOR_GRID_LINE_FREQUENCY))
					/ squareSize);
		int thickLineStartY =
			(int) ((origin.y % (squareSize * MAJOR_GRID_LINE_FREQUENCY))
					/ squareSize);
		
		for (int i = 0; i <= numSquaresHorizontal; ++i) {
			if ((i - thickLineStartX) % MAJOR_GRID_LINE_FREQUENCY == 0) {
				paint.setStrokeWidth(shouldDrawMinorLines 
						? MAJOR_GRID_LINE_WIDTH 
						: MINOR_GRID_LINE_WIDTH);
				shouldDrawCurrentLine = shouldDrawMajorLines;
			} else {
				paint.setStrokeWidth(1);
				shouldDrawCurrentLine = shouldDrawMinorLines;
			}
			
			if (shouldDrawCurrentLine) {
				canvas.drawLine(
						i * squareSize + offsetX, 0, 
						i * squareSize + offsetX, height,
						paint);
			}
		}
		
		for (int i = 0; i <= numSquaresVertical; ++i) {
			if ((i - thickLineStartY) % MAJOR_GRID_LINE_FREQUENCY == 0) {
				paint.setStrokeWidth(shouldDrawMinorLines 
						? MAJOR_GRID_LINE_WIDTH 
						: MINOR_GRID_LINE_WIDTH);
				shouldDrawCurrentLine = shouldDrawMajorLines;
			} else {
				paint.setStrokeWidth(1);
				shouldDrawCurrentLine = shouldDrawMinorLines;
			}
			
			if (shouldDrawCurrentLine) {
				canvas.drawLine(
						0, i * squareSize + offsetY, 
						width, i * squareSize + offsetY,
						paint);
			}
		}
	}
	
	//Returns nearest snap point in grid space
	@Override
	public final PointF getNearestSnapPoint(
			final PointF currentLocation, 
			final float tokenDiameter) {
		float previousGridLineX =
			(float) Math.floor((double) currentLocation.x);
		float previousGridLineY =
			(float) Math.floor((double) currentLocation.y);
		float offset = .5f * tokenDiameter
			- (float) Math.floor(.5 * tokenDiameter);
		
		// If we have a token that is smaller than one grid line, find the\
		// nearest subgrid line instead.
		if (tokenDiameter < 1) {
			previousGridLineX += (currentLocation.x - previousGridLineX)
				- (currentLocation.x - previousGridLineX) % tokenDiameter;
			previousGridLineY += (currentLocation.y - previousGridLineY)
				- (currentLocation.y - previousGridLineY) % tokenDiameter;
		}
		
		return new PointF(
				previousGridLineX + offset, previousGridLineY + offset);
	}
}
