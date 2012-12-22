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
	
	private boolean mVisibleByDefault;

	/**
	 * Constructor.
	 * @param view The view that this interaction mode modifies.
	 * @param visibleByDefault 
	 */
	public MaskDrawInteractionMode(CombatView view, boolean visibleByDefault) {
		super(view);
		mVisibleByDefault = visibleByDefault;
	}

	@Override
    protected Shape createLine() {
    	return getView().createFogOfWarRegion();
    }
	
	@Override
	public String getExplanatoryText() {
		String explanatoryText = "Editing layer mask - ";
		if (getView().getActiveFogOfWar().isEmpty()) {
			explanatoryText += (mVisibleByDefault ? "Everything is visible" : "Nothing is visible");
		} else {
			explanatoryText += "Regions in red are visible";
		}
		return explanatoryText;
	}
}
