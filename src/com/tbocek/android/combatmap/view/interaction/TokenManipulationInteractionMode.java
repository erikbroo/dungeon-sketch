package com.tbocek.android.combatmap.view.interaction;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

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
    		= new Rect(16, 16, 96 + 16, 96 + 16);
    //CHECKSTYLE:ON
    
    /**
     * Length of the trash can fade in, in ms.
     */
    private static final int TRASH_FADE_IN_DURATION = 1000;
    
    /**
     * Length of the trash can fade out, in ms.
     */
    private static final int TRASH_FADE_OUT_DURATION = 250;

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
     * Animated alpha value to use for the trash can; allows it to fade in and 
     * out.
     */
    private int mTrashCanAlpha;
    
    /**
     * Animation object to fade the trash can.
     */
    private ValueAnimator mTrashCanAnimator;
    
    /**
     * Animation update handler that changes the alpha value of the trash can.
     */
    private ValueAnimator.AnimatorUpdateListener mTrashCanFadeListener = 
    		new ValueAnimator.AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					mTrashCanAlpha = (Integer) animation.getAnimatedValue();
					getView().refreshMap();
				}
			};
    
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
                                getView().tokensSnapToIntersections() 
                        			? 0 : mCurrentToken.getSize());
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
            fadeTrashCanIn();
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
            getView().getMultiSelect().addToken(mCurrentToken);
            getView().refreshMap();
        }
    }

    @Override
    public void onUp(final MotionEvent ev) {
        mDown = false;
        getView().refreshMap();
        if (mCurrentToken != null) {
		    if (mAboutToTrash) {
		    	getView().getTokens().restoreCheckpointedTokens();
		    	getView().getTokens().remove(mCurrentToken);
		    	getView().refreshMap();
		    } else if (mMoved) {
		    	getView().getTokens().createCommandHistory();
		    }
        	fadeTrashCanOut();
        }
    }

    @Override
    public void draw(final Canvas c) {
        if (mCurrentToken != null && mDown) {
        	// Draw a ghost of the token at the location that it is being moved
        	// from.
            mCurrentToken.drawGhost(
            		c, getView().getGridSpaceTransformer(), mOriginalLocation);
        }
        
        if (mTrashCanAlpha != 0) {
            // Draw a trash can to drag tokens to.
            ensureTrashCanDrawablesCreated();
            
            if (mAboutToTrash) {
            	mTrashHoverDrawable.setAlpha(mTrashCanAlpha);
            	mTrashHoverDrawable.draw(c);
            } else {
            	mTrashDrawable.setAlpha(mTrashCanAlpha);
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
	
	/**
	 * Begins an animation to fade the trash can in.
	 */
    private void fadeTrashCanIn() {
    	if (mTrashCanAnimator != null && mTrashCanAnimator.isRunning()) {
    		mTrashCanAnimator.cancel();
    	}
		mTrashCanAnimator = ValueAnimator.ofInt(0, Util.FULL_OPACITY);
		mTrashCanAnimator.setDuration(TRASH_FADE_IN_DURATION);
		mTrashCanAnimator.addUpdateListener(mTrashCanFadeListener);
		mTrashCanAnimator.start();
	}
    
	/**
	 * Begins an animation to fade the trash can out.
	 */
    private void fadeTrashCanOut() {
    	if (mTrashCanAnimator != null && mTrashCanAnimator.isRunning()) {
    		mTrashCanAnimator.cancel();
    	}
    	mTrashCanAnimator = ValueAnimator.ofInt(this.mTrashCanAlpha, 0);
    	mTrashCanAnimator.setDuration(TRASH_FADE_OUT_DURATION);
    	mTrashCanAnimator.addUpdateListener(mTrashCanFadeListener);
    	mTrashCanAnimator.start();
	}
    

}
