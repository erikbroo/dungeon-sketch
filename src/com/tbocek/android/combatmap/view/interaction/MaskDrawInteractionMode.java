package com.tbocek.android.combatmap.view.interaction;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Defines an interaction mode where the user draws on a mask (aka Fog of War)
 * layer.
 * @author Tim
 *
 */
public final class MaskDrawInteractionMode extends FingerDrawInteractionMode {

	/**
	 * Constructor.
	 * @param view The view that this interaction mode modifies.
	 * @param visibleByDefault 
	 */
	public MaskDrawInteractionMode(CombatView view) {
		super(view);
	}

	@Override
    protected Shape createLine() {
    	return getView().createFogOfWarRegion();
    }
}
