package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.Line;

import android.graphics.PointF;
import android.view.MotionEvent;

public class FingerDrawInteractionMode extends CombatViewInteractionMode {

	public FingerDrawInteractionMode(CombatView view) {
		super(view);
	}

	private Line currentLine;
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (currentLine == null) {
			return true;
		}
		currentLine.addPoint(view.getTransformer().screenSpaceToWorldSpace(new PointF(e2.getX(), e2.getY())));
		view.invalidate();
		return true;
	}
	
	public boolean onDown(MotionEvent e) {
		currentLine = view.createLine();
		return true;
	}
}
