package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.Line;
import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.Util;

import android.view.MotionEvent;

public class FingerDrawInteractionMode extends CombatViewInteractionMode {
	private float lastPointX;
	private float lastPointY;
	public FingerDrawInteractionMode(CombatView view) {
		super(view);
	}

	private Line currentLine;
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (currentLine == null) {
			return true;
		}
		boolean DEBUG_shoulddraw = shouldDrawLine(e2.getX(), e2.getY());
		if (shouldDrawLine(e2.getX(), e2.getY())) {
			addLinePoint(e2);
		}
		return true;
	}

	private void addLinePoint(MotionEvent e2) {
		currentLine.addPoint(view.getTransformer().screenSpaceToWorldSpace(new PointF(e2.getX(), e2.getY())));
		view.invalidate();
		lastPointX = e2.getX();
		lastPointY = e2.getY();
	}
	
	private boolean shouldDrawLine(float newPointX, float newPointY) {
		return Util.distance(lastPointX, lastPointY, newPointX, newPointY) > 3;
	}
	
	public boolean onDown(MotionEvent e) {
		currentLine = view.createLine();
		lastPointX = e.getX();
		lastPointY = e.getY();
		return true;
	}
}
