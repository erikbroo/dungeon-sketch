package com.tbocek.android.combatmap.view.interaction;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Interaction mode for managing multiple selected tokens.
 * @author Tim
 *
 */
public class TokenMultiSelectInteractionMode extends ZoomPanInteractionMode {

	/**
	 * Whether the current scroll operation should drag the selected tokens
	 * or scroll the screen.
	 */
	private boolean mDragging;

    /**
     * Whether the current token has been moved.
     */
    private boolean mMoved;
    
    /**
     * Collection of ghost tokens to draw when moving a group of tokens.
     */
    private Collection<BaseToken> mUnmovedTokens = Lists.newArrayList();
	
	/**
	 * Constructor.
	 * @param view The view to manipulate.
	 */
	public TokenMultiSelectInteractionMode(CombatView view) {
		super(view);
	}

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
    	BaseToken t = getView().getTokens().getTokenUnderPoint(
        		new PointF(
        				e.getX(), e.getY()), 
        				getView().getGridSpaceTransformer());
    	if (t != null) {
	    	getView().getMultiSelect().toggleToken(t);
	    	getView().refreshMap();
    	}
        return true;
    }
    
    @Override
    public boolean onDown(final MotionEvent e) {
        BaseToken currentToken = getView().getTokens().getTokenUnderPoint(
        		new PointF(
        				e.getX(), e.getY()), 
        				getView().getGridSpaceTransformer());

        if (currentToken != null) {
        	mMoved = false;
        	mDragging = true;
            getView().getTokens().checkpointTokens(new ArrayList<BaseToken>(
            		getView().getMultiSelect().getSelectedTokens()));
            for (BaseToken t: getView().getMultiSelect().getSelectedTokens()) {
    			this.mUnmovedTokens.add(t.clone());
    		}
        } else {
        	mDragging = false;
        }

        return true;
    }
    
    @Override
    public void onUp(final MotionEvent ev) {
    	mUnmovedTokens.clear();
    	if (mMoved) {
	    	getView().getTokens().createCommandHistory();
	    }
        getView().refreshMap();
    }
    
    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
    		final float distanceX, final float distanceY) {
    	if (mDragging) {
    		mMoved = true;
    		CoordinateTransformer transformer 
    			= getView().getGridSpaceTransformer();
    		float deltaX = transformer.screenSpaceToWorldSpace(distanceX);
    		float deltaY = transformer.screenSpaceToWorldSpace(distanceY);
    		for (BaseToken t: getView().getMultiSelect().getSelectedTokens()) {
    			t.move(deltaX, deltaY);
    		}
    	} else {
    		return super.onScroll(e1, e2, distanceX, distanceY);
    	}
    	getView().refreshMap();
    	return true;
    }
    
    @Override
    public void draw(final Canvas c) {
        for (BaseToken t: mUnmovedTokens) {
        	t.drawGhost(c, getView().getGridSpaceTransformer(), 
        			t.getLocation());
        }
    }
    
}
