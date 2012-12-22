package com.tbocek.android.combatmap.view.interaction;

import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.view.CombatView;

public class MaskEraseInteractionMode extends ZoomPanInteractionMode {

	private boolean mVisibleByDefault;
	
	public MaskEraseInteractionMode(CombatView view, boolean visibleByDefault) {
		super(view);
		mVisibleByDefault = visibleByDefault;
	}
	
	@Override
	public String getExplanatoryText() {
		String explanatoryText = "Editing layer mask";
		if (getView().getActiveFogOfWar().isEmpty()) {
			explanatoryText += " - " + (mVisibleByDefault ? "Everything is visible" : "Nothing is visible");
		}
		return explanatoryText;
	}

}
