package com.tbocek.android.combatmap.view.interaction;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

public class BaseDrawInteractionMode extends CombatViewInteractionMode {

	/**
	 * The point in world space that was long-pressed to open the menu.
	 */
	private PointF longPressPoint;

	public BaseDrawInteractionMode(CombatView view) {
		super(view);
	}

	@Override
	public void onLongPress(final MotionEvent ev) {
		longPressPoint = mView.getWorldSpaceTransformer()
				.screenSpaceToWorldSpace(ev.getX(), ev.getY());
	    if (mView.isAFogOfWarLayerVisible() 
	    		&& mView.getActiveFogOfWar() != null 
	    		&& mView.getActiveFogOfWar().findShape(longPressPoint) != null) {
	        mView.showContextMenu();
	    }
	}

	/**
	 * Allows the interaction mode to specify custom context menu options.
	 * @param menu The context menu to populate
	 */
	public void onCreateContextMenu(final ContextMenu menu) {
		  menu.add(Menu.NONE, R.id.fog_context_delete,
	     		 Menu.NONE, "Delete Fog Of War Region");
	  }

	/**
	 * Allows the interaction mode to specify the actions taken in
	 * response to context menu items that it added.
	 * @param item The context menu item clicked.
	 * @return Whether the event was handled.
	 */
	public boolean onContextItemSelected(final MenuItem item) {
	      if (item.getItemId() == R.id.fog_context_delete 
	    		  && mView.getActiveFogOfWar() != null) {
	    	  mView.getActiveFogOfWar().deleteShape(
	    			  mView.getActiveFogOfWar().findShape(longPressPoint));
	    	  return true;
	      }
	      return false;
	}

}