package com.tbocek.android.combatmap.model;

import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;

import android.graphics.Canvas;
import android.graphics.Paint;



/**
 * A grid based on tesselated hexagons.
 *
 * @author Tim Bocek
 */
public final class HexGridStrategy extends GridDrawStrategy {
    /**
     * Cached result for the tan(30 degrees).
     */
    private static final float TANGENT_30_DEGREES =
    	//CHECKSTYLE:OFF
        (float) Math.tan(30 * Math.PI / 180);
    	//CHECKSTYLE:ON

    /**
     * Square size at which grid elements will stop being drawn.
     */
    private static final float MIN_SQUARE_SIZE = 15;

    @Override
    public PointF getNearestSnapPoint(final PointF currentLocation,
            final float tokenDiameter) {
        float h = .5f * TANGENT_30_DEGREES;
        float previousGridLineX =
            (float) Math.floor((double) currentLocation.x) + h / 2;
        float previousGridLineY =
            (float) Math.floor((double) currentLocation.y);

        if (((int) previousGridLineX) % 2 == 0) {
            previousGridLineY -= .5;
        }

        float offset =
            .5f * tokenDiameter - (float) Math.floor(.5 * tokenDiameter);

        // If we have a token that is smaller than one grid line, find the
        // nearest subgrid line instead.
        if (tokenDiameter < 1) {
            previousGridLineX +=
                (currentLocation.x - previousGridLineX)
                - (currentLocation.x - previousGridLineX) % tokenDiameter;
            previousGridLineY +=
                (currentLocation.y - previousGridLineY)
                - (currentLocation.y - previousGridLineY) % tokenDiameter;
        }

        return new PointF(
            previousGridLineX + offset, previousGridLineY + offset);
    }

    @Override
    public void drawGrid(final Canvas canvas,
            final CoordinateTransformer transformer,
            final GridColorScheme colorScheme) {
        Paint paint = new Paint();
        paint.setColor(colorScheme.getLineColor());
        
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float squareSize = transformer.worldSpaceToScreenSpace(1.0f);

        if (squareSize < MIN_SQUARE_SIZE) {
        	return;
        }

        float numSquaresHorizontal = (float) width / squareSize;
        float numSquaresVertical =
            numSquaresHorizontal * ((float) height) / ((float) width);

        PointF origin = transformer.getOrigin();

        float offsetX = origin.x % squareSize;
        float offsetY = origin.y % squareSize;

        int innerPointStartX =
            (int) ((origin.x % (squareSize * 2)) / squareSize);


        float h = .5f * squareSize * TANGENT_30_DEGREES;

        // Draw the vertical undulating "lines".  We want to start slightly
        // offscreen
        for (int j = -1; j <= numSquaresHorizontal + 1; ++j) {
            float x = j * squareSize + offsetX;
            float innerX = x + h;
            for (int i = -1; i <= numSquaresVertical + 1; ++i) {
                float y = i * squareSize + offsetY
                    - (j - innerPointStartX) % 2 * .5f * squareSize;
                canvas.drawLine(x, y, innerX, y + squareSize / 2, paint);
                canvas.drawLine(
                    innerX, y + squareSize / 2, x, y + squareSize, paint);
                canvas.drawLine(
                    innerX, y + squareSize / 2, innerX + squareSize - h,
                    y + squareSize / 2, paint);
            }
        }
    }

	@Override
	public String getTypeString() {
		return "rect";
	}

}
