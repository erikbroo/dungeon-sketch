package com.tbocek.android.combatmap.view.interaction;

import android.view.MotionEvent;

import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Interaction mode for managing multiple selected tokens.
 * @author Tim
 *
 */
public class TokenMultiSelectInteractionMode extends CombatViewInteractionMode {

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
}
