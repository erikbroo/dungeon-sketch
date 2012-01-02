package com.tbocek.android.combatmap.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.Util;

/**
 * An interaction mode that allows the user to drag tokens around the canvas.
 * @author Tim Bocek
 *
 */
public final class TokenManipulationInteractionMode
		extends ZoomPanInteractionMode {
	/**
	 * Distance in screen space that a token needs to be away from a snap point
	 * until it will snap.
	 */
    private static final int GRID_SNAP_THRESHOLD = 20;

    /**
     * Constructor.
     * @param view The CombatView that this mode will interact with.
     */
    public TokenManipulationInteractionMode(final CombatView view) {
        super(view);
    }

    /**
     * The token currently being dragged around.
     */
    private BaseToken currentToken = null;

    /**
     * The original location of the token being dragged around.
     */
    private PointF originalLocation;

    /**
     * Whether the use is currently dragging a token.
     */
    private boolean down = false;

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
        if (currentToken != null) {
            CoordinateTransformer transformer = view.getGridSpaceTransformer();
            PointF currentPointScreenSpace = new PointF(e2.getX(), e2.getY());
            if (view.shouldSnapToGrid()) {
                // Get the nearest snap point in screen space
                PointF nearestSnapPointWorldSpace =
                	view.getData().getGrid().getNearestSnapPoint(
                        transformer.screenSpaceToWorldSpace(
                                currentPointScreenSpace),
                        currentToken.getSize());
                // Snap to that point if it is less than a threshold
                float distanceToSnapPoint = Util.distance(
                        transformer.worldSpaceToScreenSpace(
                        		nearestSnapPointWorldSpace),
                        currentPointScreenSpace);

                currentToken.setLocation(
                		distanceToSnapPoint < GRID_SNAP_THRESHOLD
                		? nearestSnapPointWorldSpace
                		: transformer.screenSpaceToWorldSpace(
                				currentPointScreenSpace));
            } else {
                currentToken.setLocation(
                		transformer.screenSpaceToWorldSpace(
                				currentPointScreenSpace));
            }
        } else {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        view.refreshMap();
        return true;
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        currentToken = view.getTokens().getTokenUnderPoint(
        		new PointF(e.getX(), e.getY()), view.getGridSpaceTransformer());

        if (currentToken != null) {
            originalLocation = currentToken.getLocation();
        }

        down = true;
        return true;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (currentToken != null) {
            currentToken.setBloodied(!currentToken.isBloodied());
        }
        view.refreshMap();
        return true;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        if (currentToken != null) {
            view.showContextMenu();
        }
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu) {
        if (currentToken != null) {
            menu.add(Menu.NONE, R.id.token_context_delete_token,
            		 Menu.NONE, "Delete Token");
            
            SubMenu sm = menu.addSubMenu("Change Size");
            sm.add(Menu.NONE, R.id.token_context_size_tenth, 1, "1/10");
            sm.add(Menu.NONE, R.id.token_context_size_quarter, 2, "1/4");
            sm.add(Menu.NONE, R.id.token_context_size_half, 3, "1/2");
            sm.add(Menu.NONE, R.id.token_context_size_one, 4, "1");
            sm.add(Menu.NONE, R.id.token_context_size_two, 5, "2");
            sm.add(Menu.NONE, R.id.token_context_size_three, 6, "3");
            sm.add(Menu.NONE, R.id.token_context_size_four, 7, "4");
            sm.add(Menu.NONE, R.id.token_context_size_five, 8, "5");
            sm.add(Menu.NONE, R.id.token_context_size_six, 9, "6");
            
            sm = menu.addSubMenu("Border Color");
            sm.add(Menu.NONE, R.id.token_border_none, 1, "No Border");
            sm.add(Menu.NONE, R.id.token_border_white, 1, "White");
            sm.add(Menu.NONE, R.id.token_border_black, 1, "Back");
            sm.add(Menu.NONE, R.id.token_border_blue, 1, "Blue");
            sm.add(Menu.NONE, R.id.token_border_red, 1, "Red");
            sm.add(Menu.NONE, R.id.token_border_green, 1, "Green");
            sm.add(Menu.NONE, R.id.token_border_yellow, 1, "Yellow");
        }

    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.token_context_delete_token:
            view.getTokens().remove(currentToken);
            return true;
        case R.id.token_context_size_tenth:
            currentToken.setSize(.1f);
            return true;
        case R.id.token_context_size_quarter:
            currentToken.setSize(.25f);
            return true;
        case R.id.token_context_size_half:
            currentToken.setSize(.5f);
            return true;
        case R.id.token_context_size_one:
            currentToken.setSize(1);
            return true;
        case R.id.token_context_size_two:
            currentToken.setSize(2);
            return true;
        case R.id.token_context_size_three:
            currentToken.setSize(3);
            return true;
        case R.id.token_context_size_four:
            currentToken.setSize(4);
            return true;
        case R.id.token_context_size_five:
            currentToken.setSize(5);
            return true;
        case R.id.token_context_size_six:
            currentToken.setSize(6);
            return true;
        case R.id.token_border_none:
        	currentToken.clearCustomBorderColor();
        	return true;
        case R.id.token_border_white:
        	currentToken.setCustomBorder(Color.WHITE);
        	return true;
        case R.id.token_border_black:
        	currentToken.setCustomBorder(Color.BLACK);
        	return true;
        case R.id.token_border_blue:
        	currentToken.setCustomBorder(Color.BLUE);
        	return true;
        case R.id.token_border_red:
        	currentToken.setCustomBorder(Color.RED);
        	return true;
        case R.id.token_border_green:
        	currentToken.setCustomBorder(Color.GREEN);
        	return true;
        case R.id.token_border_yellow:
        	currentToken.setCustomBorder(Color.YELLOW);
        	return true;
        default:
        	return false;
        }
    }

    @Override
    public void onUp(final MotionEvent ev) {
        down = false;
        view.refreshMap();
    }

    @Override
    public void draw(final Canvas c) {
        if (currentToken != null && down) {
            currentToken.drawGhost(
            		c, view.getGridSpaceTransformer(), originalLocation);
        }
    }
}
