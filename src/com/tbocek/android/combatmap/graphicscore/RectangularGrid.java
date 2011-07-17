package com.tbocek.android.combatmap.graphicscore;

import android.graphics.Canvas;
import android.graphics.Paint;

public class RectangularGrid extends Grid {
	
	@Override
	protected void drawGrid(Canvas canvas, CoordinateTransformer worldToScreenTransformer) {
		Paint paint = new Paint();
		paint.setColor(colorScheme.getLineColor());
		
		CoordinateTransformer transformer = gridSpaceToScreenSpaceTransformer(worldToScreenTransformer);
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		float squareSize = transformer.worldSpaceToScreenSpace(1.0f);
		float numSquaresHorizontal = (float)width/squareSize;
		float numSquaresVertical = (numSquaresHorizontal * ((float)height)/((float)width));
		
		boolean shouldDrawMinorLines = squareSize >= 8;
		boolean shouldDrawMajorLines = squareSize >= 4;
		boolean shouldDrawCurrentLine = true;
		
		PointF origin = transformer.getOrigin();
		
		float offsetX = origin.x % squareSize;
		float offsetY = origin.y % squareSize;
		
		int thickLineStartX = (int)((origin.x % (squareSize * 5)) / squareSize);
		int thickLineStartY = (int)((origin.y % (squareSize * 5)) / squareSize);
		
		for (int i = 0; i <= numSquaresHorizontal; ++i) {
			if ((i-thickLineStartX)%5 == 0) {
				paint.setStrokeWidth(shouldDrawMinorLines ? 3 : 1);
				shouldDrawCurrentLine = shouldDrawMajorLines;
			}
			else {
				paint.setStrokeWidth(1);
				shouldDrawCurrentLine = shouldDrawMinorLines;
			}
			
			if (shouldDrawCurrentLine) {
				canvas.drawLine(i * squareSize + offsetX, 0, i * squareSize + offsetX, height, paint);
			}
		}
		
		for (int i = 0; i <= numSquaresVertical; ++i) {
			if ((i-thickLineStartY)%5 == 0) {
				paint.setStrokeWidth(shouldDrawMinorLines ? 3 : 1);
				shouldDrawCurrentLine = shouldDrawMajorLines;
			}
			else {
				paint.setStrokeWidth(1);
				shouldDrawCurrentLine = shouldDrawMinorLines;
			}
			
			if (shouldDrawCurrentLine) {
				canvas.drawLine(0, i * squareSize + offsetY, width, i * squareSize + offsetY, paint);
			}
		}
	}
	
	//Returns nearest snap point in grid space
	@Override
	public PointF getNearestSnapPoint(PointF currentLocation, float tokenDiameter) {
		float previousGridLineX = (float) Math.floor((double) currentLocation.x);
		float previousGridLineY = (float) Math.floor((double) currentLocation.y);
		float offset = .5f * tokenDiameter - (float)Math.floor(.5 * tokenDiameter);
		
		// If we have a token that is smaller than one grid line, find the nearest subgrid
		// line instead.
		if (tokenDiameter < 1) {
			previousGridLineX += (currentLocation.x - previousGridLineX) - (currentLocation.x - previousGridLineX) % tokenDiameter;
			previousGridLineY += (currentLocation.y - previousGridLineY) - (currentLocation.y - previousGridLineY) % tokenDiameter;
		}
		
		return new PointF(previousGridLineX + offset, previousGridLineY + offset);
	}
}
