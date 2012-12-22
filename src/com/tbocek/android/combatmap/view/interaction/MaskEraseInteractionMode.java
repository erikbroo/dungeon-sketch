package com.tbocek.android.combatmap.view.interaction;

import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.view.CombatView;

public class MaskEraseInteractionMode extends ZoomPanInteractionMode {

	private boolean mVisibleByDefault;
	
	public MaskEraseInteractionMode(CombatView view) {
		super(view);
	}
}
