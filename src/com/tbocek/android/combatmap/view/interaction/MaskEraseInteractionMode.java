package com.tbocek.android.combatmap.view.interaction;

import android.view.MotionEvent;

import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.view.CombatView;

public class MaskEraseInteractionMode extends ZoomPanInteractionMode {
	public MaskEraseInteractionMode(CombatView view) {
		super(view);
	}

	@Override
	public boolean onSingleTapUp(final MotionEvent ev) {
		PointF pt = getView().getWorldSpaceTransformer()
				.screenSpaceToWorldSpace(ev.getX(), ev.getY());
		if (getView().isAFogOfWarLayerVisible()
				&& getView().getActiveFogOfWar() != null) {
			Shape shapeUnderPress = getView().getActiveFogOfWar().findShape(pt);
			if (shapeUnderPress != null) {
				getView().getActiveFogOfWar().deleteShape(shapeUnderPress);
				getView().refreshMap();
			}
		}
		return true;
	}
}
