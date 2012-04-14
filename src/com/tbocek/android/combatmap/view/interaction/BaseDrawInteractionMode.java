package com.tbocek.android.combatmap.view.interaction;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * Base class for drawing interaction modes that implements some common behavior
 * that should always happen when drawing.
 * @author Tim
 *
 */
public class BaseDrawInteractionMode extends CombatViewInteractionMode {

	/**
	 * The point in world space that was long-pressed to open the menu.
	 */
	private PointF mLongPressPoint;

	/**
	 * Constructor.
	 * @param view The CombatView to manipulate.
	 */
	public BaseDrawInteractionMode(CombatView view) {
		super(view);
	}

	@Override
	public void onLongPress(final MotionEvent ev) {
		mLongPressPoint = getView().getWorldSpaceTransformer()
				.screenSpaceToWorldSpace(ev.getX(), ev.getY());
	    if (getView().isAFogOfWarLayerVisible() 
	    		&& getView().getActiveFogOfWar() != null 
	    		&& getView().getActiveFogOfWar().findShape(mLongPressPoint) 
	    				!= null) {
	        getView().showContextMenu();
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
	    		  && getView().getActiveFogOfWar() != null) {
	    	  getView().getActiveFogOfWar().deleteShape(
	    			  getView().getActiveFogOfWar().findShape(mLongPressPoint));
	    	  return true;
	      }
	      return false;
	}
	
    /**
     * Gets the draw location in screen space.  Snaps to the grid if necessary.
     * @param e The motion event to get the point from.
     * @return The point in screen space.
     */
    protected PointF getScreenSpacePoint(final MotionEvent e) {
    	PointF p = new PointF(e.getX(), e.getY());
    	if (getView().shouldSnapToGrid()) {
    		CoordinateTransformer transformer
    				= getView().getGridSpaceTransformer();
    		p = transformer.worldSpaceToScreenSpace(
    				getView().getData().getGrid().getNearestSnapPoint(
    						transformer.screenSpaceToWorldSpace(p), 0));
    	}
    	return p;
    }

}