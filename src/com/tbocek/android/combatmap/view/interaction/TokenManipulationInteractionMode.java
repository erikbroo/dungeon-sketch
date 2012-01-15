package com.tbocek.android.combatmap.view.interaction;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
     * Rectangle in which to draw the trash can.
     */
    //CHECKSTYLE:OFF
    private static final Rect TRASH_CAN_RECT 
    		= new Rect(16, 16, 128 + 16, 128 + 16);
    //CHECKSTYLE:ON

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
    
    /**
     * Cached drawable for a trash can to move tokens to.
     */
    private Drawable mTrashDrawable;
    
    /**
     * Cached drawable for the trash can image when a token is dragged to it.
     */
    private Drawable mTrashHoverDrawable;
    
    /**
     * True if the token is being hovered over the trash can.
     */
    private boolean mAboutToTrash;
    
    /**
     * Cached value of whether drawing against a dark background.  Used to 
     * detect theme changes.
     */
    private boolean mCachedDark;
    
    /**
     * Constructor.
     * @param view The CombatView that this mode will interact with.
     */
    public TokenManipulationInteractionMode(final CombatView view) {
        super(view);
    }

    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
        if (mCurrentToken != null) {
        	mMoved = true;
            CoordinateTransformer transformer 
            		= getView().getGridSpaceTransformer();
            PointF currentPointScreenSpace = new PointF(e2.getX(), e2.getY());
            if (getView().shouldSnapToGrid()) {
                // Get the nearest snap point in screen space
                PointF nearestSnapPointWorldSpace =
                	getView().getData().getGrid().getNearestSnapPoint(
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
            mAboutToTrash = TRASH_CAN_RECT.contains(
            		(int) e2.getX(), (int) e2.getY());
        } else {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        getView().refreshMap();
        return true;
    }
    
    @Override
    public boolean onDown(final MotionEvent e) {
        mCurrentToken = getView().getTokens().getTokenUnderPoint(
        		new PointF(
        				e.getX(), e.getY()), 
        				getView().getGridSpaceTransformer());

        if (mCurrentToken != null) {
        	mMoved = false;
            mOriginalLocation = mCurrentToken.getLocation();
            getView().getTokens().checkpointToken(mCurrentToken);
        }

        mDown = true;
        return true;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (mCurrentToken != null) {
        	getView().getTokens().checkpointToken(mCurrentToken);
            mCurrentToken.setBloodied(!mCurrentToken.isBloodied());
            getView().getTokens().createCommandHistory();
        }
        getView().refreshMap();
        return true;
    }

    @Override
    public void onLongPress(final MotionEvent e) {
        if (mCurrentToken != null) {
            getView().showContextMenu();
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
            getView().getTokens().remove(mCurrentToken);
            return true;
        case R.id.token_context_size_tenth:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(.1f);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_quarter:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(.25f);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_half:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(.5f);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_one:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(1);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_two:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(2);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_three:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(3);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_four:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(4);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_five:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(5);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_context_size_six:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	//CHECKSTYLE:OFF
            mCurrentToken.setSize(6);
            //CHECKSTYLE:ON
            getView().getTokens().createCommandHistory();
            return true;
        case R.id.token_border_none:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.clearCustomBorderColor();
        	getView().getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_white:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.WHITE);
        	getView().getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_black:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.BLACK);
        	getView().getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_blue:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.BLUE);
        	getView().getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_red:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.RED);
        	getView().getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_green:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.GREEN);
        	getView().getTokens().createCommandHistory();
        	return true;
        case R.id.token_border_yellow:
        	getView().getTokens().checkpointToken(mCurrentToken);
        	mCurrentToken.setCustomBorder(Color.YELLOW);
        	getView().getTokens().createCommandHistory();
        	return true;
        default:
        	return false;
        }
    }

    @Override
    public void onUp(final MotionEvent ev) {
        mDown = false;
        getView().refreshMap();
        if (mAboutToTrash) {
        	getView().getTokens().restoreCheckpointedToken();
        	getView().getTokens().remove(mCurrentToken);
        	getView().refreshMap();
        } else if (mMoved) {
        	getView().getTokens().createCommandHistory();
        }
        mCurrentToken = null;
    }

    @Override
    public void draw(final Canvas c) {
        if (mCurrentToken != null && mDown) {
        	// Draw a ghost of the token at the location that it is being moved
        	// from.
            mCurrentToken.drawGhost(
            		c, getView().getGridSpaceTransformer(), mOriginalLocation);
            // Draw a trash can to drag tokens to.
            ensureTrashCanDrawablesCreated();
            
            if (mAboutToTrash) {
            	mTrashHoverDrawable.draw(c);
            } else {
            	mTrashDrawable.draw(c);
            }
        }
    }

	/**
	 * Checks whether trash can drawables need to be created, and creates them
	 * if needed.
	 */
	private void ensureTrashCanDrawablesCreated() {
		if (mTrashDrawable == null 
				|| mCachedDark != getView().getData().getGrid().isDark()) {
			if (getView().getData().getGrid().isDark()) {
				mTrashDrawable = getView().getResources().getDrawable(
						R.drawable.trashcan);
			} else {
				mTrashDrawable = getView().getResources().getDrawable(
						R.drawable.trashcan_dark);
			}
			
			mCachedDark = getView().getData().getGrid().isDark();
		    mTrashDrawable.setBounds(TRASH_CAN_RECT);
		    mTrashHoverDrawable = getView().getResources().getDrawable(
		    		R.drawable.trashcan_hover_over);
		    mTrashHoverDrawable.setBounds(TRASH_CAN_RECT);
		}
	}
}
