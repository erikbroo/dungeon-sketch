package com.tbocek.android.combatmap.view;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.Token;
import com.tbocek.android.combatmap.graphicscore.Util;

public class TokenManipulationInteractionMode extends ZoomPanInteractionMode {
	private static final int GRID_SNAP_THRESHOLD = 20;
	public TokenManipulationInteractionMode(CombatView view) {
		super(view);
		// TODO Auto-generated constructor stub
	}

	Token currentToken = null;
	private PointF originalLocation;
	boolean down = false;
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {	
    	if (currentToken != null) {
    		CoordinateTransformer transformer = view.getGridSpaceTransformer();
    		PointF currentPointScreenSpace = new PointF(e2.getX(), e2.getY());
    		if (view.shouldSnapToGrid) {
	    		// Get the nearest snap point in screen space
	    		PointF nearestSnapPointWorldSpace = view.mData.grid.getNearestSnapPoint(
	    				transformer.screenSpaceToWorldSpace(
	    						currentPointScreenSpace),
	    				currentToken.size);
	    		// Snap to that point if it is less than a threshold
	    		float distanceToSnapPoint = Util.distance(
	    				transformer.worldSpaceToScreenSpace(nearestSnapPointWorldSpace),
	    				currentPointScreenSpace);
	    		
	    		currentToken.location = distanceToSnapPoint < GRID_SNAP_THRESHOLD
	    			? nearestSnapPointWorldSpace
	    			: transformer.screenSpaceToWorldSpace(currentPointScreenSpace);
    		} else {
    			currentToken.location = transformer.screenSpaceToWorldSpace(currentPointScreenSpace);
    		}
    	}
    	else {
    		return super.onScroll(e1, e2, distanceX, distanceY);
    	}
    	view.invalidate();
		return true;
	}
    public boolean onDown(MotionEvent e) {
    	currentToken = view.getTokens().getTokenUnderPoint(new PointF(e.getX(), e.getY()), view.getGridSpaceTransformer());
    	
    	if (currentToken != null)
    		originalLocation = currentToken.location;
    	
    	down = true;
    	return true;
    }
    
    public boolean onDoubleTap(MotionEvent e) {
    	if (currentToken != null)
    		currentToken.bloodied = !currentToken.bloodied;
    	view.invalidate();
    	return true;
    }
    
    public void onLongPress(MotionEvent e) {
    	if (currentToken != null) {
    		view.showContextMenu();
    	}
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu) {
    	if (currentToken != null) {
    		menu.add(menu.NONE, R.id.token_context_delete_token, menu.NONE, "Delete Token");
    		SubMenu sm = menu.addSubMenu("Change Size");
    		sm.add(menu.NONE, R.id.token_context_size_tenth, 1, "1/10");
    		sm.add(menu.NONE, R.id.token_context_size_quarter, 2, "1/4");
    		sm.add(menu.NONE, R.id.token_context_size_half, 3, "1/2");
    		sm.add(menu.NONE, R.id.token_context_size_one, 4, "1");
    		sm.add(menu.NONE, R.id.token_context_size_two, 5, "2");
    		sm.add(menu.NONE, R.id.token_context_size_four, 6, "4");
    		
    	}
    	
    }
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.token_context_delete_token:
			view.getTokens().remove(currentToken);
			return true;
		case R.id.token_context_size_tenth:
			currentToken.setDiameter(.1f);
			return true;
		case R.id.token_context_size_quarter:
			currentToken.setDiameter(.25f);
			return true;
		case R.id.token_context_size_half:
			currentToken.setDiameter(.5f);
			return true;
		case R.id.token_context_size_one:
			currentToken.setDiameter(1);
			return true;
		case R.id.token_context_size_two:
			currentToken.setDiameter(2);
			return true;
		case R.id.token_context_size_four:
			currentToken.setDiameter(4);
			return true;
		}
		return false;
	}
    
    public void onUp(MotionEvent ev) {
    	down = false;
    	view.invalidate();
    }
    
	public void draw(Canvas c) {
		if (currentToken != null && down) {
			currentToken.drawGhost(c, view.getGridSpaceTransformer(), originalLocation);
		}
	}
}
