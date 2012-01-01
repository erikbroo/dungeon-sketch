package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.Shape;
import com.tbocek.android.combatmap.graphicscore.Text;

import android.view.MotionEvent;
import android.view.View;

public class DrawTextInteractionMode extends BaseDrawInteractionMode {
	
	public DrawTextInteractionMode(CombatView view) {
		super(view);
	}
	
	@Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
    	PointF p = new PointF(e.getX(), e.getY());
    	
    	view.requestNewTextEntry( 
    			view.getWorldSpaceTransformer().screenSpaceToWorldSpace(p));
    	
        return true;
    }
	
    @Override
    public void onLongPress(final MotionEvent e) {
    	PointF p = view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
    			new PointF(e.getX(), e.getY()));
    	
    	Shape t = view.getActiveLines().findShape(p, Text.class);
    	if (t != null) {
    		view.requestEditTextObject((Text) t);
    	}
    }

    // Drag to move text.
    @Override
    public boolean onScroll(final MotionEvent arg0, final MotionEvent arg1,
  		  final float arg2, final float arg3) {
    	PointF p = view.getWorldSpaceTransformer().screenSpaceToWorldSpace(
    			new PointF(arg0.getX(), arg0.getY()));
    	Shape t = view.getActiveLines().findShape(p, Text.class);
    	if (t != null) {
    		t.setDrawOffset(
    			view.getWorldSpaceTransformer().screenSpaceToWorldSpace(arg1.getX() - arg0.getX()),
    			view.getWorldSpaceTransformer().screenSpaceToWorldSpace(arg1.getY() - arg0.getY()));
    		view.refreshMap();
    		return true;
    	} else {
    		return super.onScroll(arg0, arg1, arg2, arg3);
    	}
    }
    
    public void onUp(final MotionEvent event) {
    	view.getActiveLines().optimize();
    	view.refreshMap();
    }
	
	@Override
    public void onStartMode() {
		Text.drawBoundingBoxes = true;
    }
	
	@Override
    public void onEndMode() {
		Text.drawBoundingBoxes = false;
    }
}
