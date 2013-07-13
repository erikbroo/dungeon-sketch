package com.tbocek.android.combatmap.view.interaction;

import android.view.MotionEvent;

import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

public class MeasuringTapeInteractionMode extends BaseDrawInteractionMode {

	private float mLastPointX;
	private float mLastPointY;
	private boolean measuring = false;
	public MeasuringTapeInteractionMode(CombatView view) {
		super(view);
	}
    @Override
    public boolean onDown(final MotionEvent e) {
        PointF p = this.getScreenSpacePoint(e);
        this.mLastPointX = p.x;
        this.mLastPointY = p.y;
        measuring = true;
        return true;
    }
    
    public void onUp(final MotionEvent e) {
        if (this.getNumberOfFingers() == 0) {
            this.measuring =  false;
        }
    }
}
