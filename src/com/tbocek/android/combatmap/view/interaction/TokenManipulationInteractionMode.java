package com.tbocek.android.combatmap.view.interaction;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.view.CombatView;

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
    private BaseToken currentToken;

    /**
     * The original location of the token being dragged around.
     */
    private PointF originalLocation;

    /**
     * Whether the use is currently dragging a token.
     */
    private boolean down;
    
    private boolean moved;

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
        if (currentToken != null) {
        	moved = true;
            CoordinateTransformer transformer = mView.getGridSpaceTransformer();
            PointF currentPointScreenSpace = new PointF(e2.getX(), e2.getY());
            if (mView.shouldSnapToGrid()) {
                // Get the nearest snap point in screen space
                PointF nearestSnapPointWorldSpace =
                	mView.getData().getGrid().getNearestSnapPoint(
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
        mView.refreshMap();
        return true;
    }
    
    @Override
    public boolean onDown(final MotionEvent e) {
        currentToken = mView.getTokens().getTokenUnderPoint(
        		new PointF(e.getX(), e.getY()), mView.getGridSpaceTransformer());

        if (currentToken != null) {
        	moved = false;
            originalLocation = currentToken.getLocation();
            mView.getTokens().checkpointToken(currentToken);
        }

        down = true;
        return true;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (currentToken != null) {
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setBloodied(!currentToken.isBloodied());
            mView.getTokens().createCommandHistory();
        }
        mView.refreshMap();
        return true;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        if (currentToken != null) {
            mView.showContextMenu();
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
            mView.getTokens().remove(currentToken);
            return true;
        case R.id.token_context_size_tenth:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(.1f);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_quarter:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(.25f);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_half:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(.5f);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_one:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(1);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_two:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(2);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_three:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(3);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_four:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(4);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_five:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(5);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_six:
        	mView.getTokens().checkpointToken(currentToken);
            currentToken.setSize(6);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_border_none:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.clearCustomBorderColor();
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_white:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.setCustomBorder(Color.WHITE);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_black:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.setCustomBorder(Color.BLACK);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_blue:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.setCustomBorder(Color.BLUE);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_red:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.setCustomBorder(Color.RED);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_green:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.setCustomBorder(Color.GREEN);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_yellow:
        	mView.getTokens().checkpointToken(currentToken);
        	currentToken.setCustomBorder(Color.YELLOW);
        	mView.getTokens().createCommandHistory();
        	return true;
        default:
        	return false;
        }
    }

    @Override
    public void onUp(final MotionEvent ev) {
        down = false;
        mView.refreshMap();
        if (moved) {
        	mView.getTokens().createCommandHistory();
        }
    }

    @Override
    public void draw(final Canvas c) {
        if (currentToken != null && down) {
            currentToken.drawGhost(
            		c, mView.getGridSpaceTransformer(), originalLocation);
        }
    }
}
