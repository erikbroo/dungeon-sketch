package com.tbocek.android.combatmap.view.interaction;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.ContextMenu;
import android.view.Menu;
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
    private BaseToken mCurrentToken;

    /**
     * The original location of the token being dragged around.
     */
    private PointF mOriginalLocation;

    /**
     * Whether the use is currently dragging a token.
     */
    private boolean mDown;
    
    /**
     * Whether the current token has been moved.
     */
    private boolean mMoved;

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
        if (mCurrentToken != null) {
        	mMoved = true;
            CoordinateTransformer transformer = mView.getGridSpaceTransformer();
            PointF currentPointScreenSpace = new PointF(e2.getX(), e2.getY());
            if (mView.shouldSnapToGrid()) {
                // Get the nearest snap point in screen space
                PointF nearestSnapPointWorldSpace =
                	mView.getData().getGrid().getNearestSnapPoint(
                        transformer.screenSpaceToWorldSpace(
                                currentPointScreenSpace),
                        mCurrentToken.getSize());
                // Snap to that point if it is less than a threshold
                float distanceToSnapPoint = Util.distance(
                        transformer.worldSpaceToScreenSpace(
                        		nearestSnapPointWorldSpace),
                        currentPointScreenSpace);

                mCurrentToken.setLocation(
                		distanceToSnapPoint < GRID_SNAP_THRESHOLD
                		? nearestSnapPointWorldSpace
                		: transformer.screenSpaceToWorldSpace(
                				currentPointScreenSpace));
            } else {
                mCurrentToken.setLocation(
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
        mCurrentToken = mView.getTokens().getTokenUnderPoint(
        		new PointF(
        				e.getX(), e.getY()), mView.getGridSpaceTransformer());

        if (mCurrentToken != null) {
        	mMoved = false;
            mOriginalLocation = mCurrentToken.getLocation();
            mView.getTokens().checkpointToken(mCurrentToken);
        }

        mDown = true;
        return true;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (mCurrentToken != null) {
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setBloodied(!mCurrentToken.isBloodied());
            mView.getTokens().createCommandHistory();
        }
        mView.refreshMap();
        return true;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        if (mCurrentToken != null) {
            mView.showContextMenu();
        }
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu) {
        if (mCurrentToken != null) {
            menu.add(Menu.NONE, R.id.token_context_delete_token,
            		 Menu.NONE, "Delete Token");
            
            SubMenu sm = menu.addSubMenu("Change Size");
            // CHECKSTYLE:OFF
            sm.add(Menu.NONE, R.id.token_context_size_tenth, 1, "1/10");
            sm.add(Menu.NONE, R.id.token_context_size_quarter, 2, "1/4");
            sm.add(Menu.NONE, R.id.token_context_size_half, 3, "1/2");
            sm.add(Menu.NONE, R.id.token_context_size_one, 4, "1");
            sm.add(Menu.NONE, R.id.token_context_size_two, 5, "2");
            sm.add(Menu.NONE, R.id.token_context_size_three, 6, "3");
            sm.add(Menu.NONE, R.id.token_context_size_four, 7, "4");
            sm.add(Menu.NONE, R.id.token_context_size_five, 8, "5");
            sm.add(Menu.NONE, R.id.token_context_size_six, 9, "6");
            // CHECKSTYLE:ON
            
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
            mView.getTokens().remove(mCurrentToken);
            return true;
        case R.id.token_context_size_tenth:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(.1f);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_quarter:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(.25f);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_half:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(.5f);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_one:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(1);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_two:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(2);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_three:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(3);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_four:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(4);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_five:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(5);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_six:
        	mView.getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setSize(6);
            mView.getTokens().createCommandHistory();
            return true;
        case R.id.token_border_none:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.clearCustomBorderColor();
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_white:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.WHITE);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_black:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.BLACK);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_blue:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.BLUE);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_red:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.RED);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_green:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.GREEN);
        	mView.getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_yellow:
        	mView.getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.YELLOW);
        	mView.getTokens().createCommandHistory();
        	return true;
        default:
        	return false;
        }
    }

    @Override
    public void onUp(final MotionEvent ev) {
        mDown = false;
        mView.refreshMap();
        if (mMoved) {
        	mView.getTokens().createCommandHistory();
        }
    }

    @Override
    public void draw(final Canvas c) {
        if (mCurrentToken != null && mDown) {
            mCurrentToken.drawGhost(
            		c, mView.getGridSpaceTransformer(), mOriginalLocation);
        }
    }
}
