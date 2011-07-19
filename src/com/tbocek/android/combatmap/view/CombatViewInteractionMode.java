package com.tbocek.android.combatmap.view;


import com.tbocek.android.combatmap.graphicscore.PointF;

import android.graphics.Canvas;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * This class roots a strategy pattern hierarchy that primarily defines actions
 * to take on the CombatView given a particular gesture.  These have been
 * extracted so that different interaction modes (panning, drawing, moving
 * tokens, etc) can easily and modularly be defined.
 *
 * @author Tim Bocek
 */
public class CombatViewInteractionMode extends SimpleAllGestureListener {

    /**
     * The CombatView that this interaction mode manipulates.
     */
    protected CombatView view;

    /**
     * Constructor
     * @param view The CombatView that this interaction mode manipulates.
     */
	  public CombatViewInteractionMode(CombatView view) {
	      this.view = view;
	  }

	  @Override
	  public boolean onScale(ScaleGestureDetector detector) {
        view.getTransformer().zoom(
            detector.getScaleFactor(),
            new PointF(detector.getFocusX(), detector.getFocusY()));
        view.invalidate();
        return true;
	  }

    /**
     * Allows the interaction mode to specify custom context menu options.
     * @param menu The context menu to populate
     */
	  public void onCreateContextMenu(ContextMenu menu) {
	  }

    /**
     * Allows the interaction mode to specify the actions taken in
     * response to context menu items that it added.
     * @param item The context menu item clicked.
     */
	  public boolean onContextItemSelected(MenuItem item) {
	      return false;
    }
 
	  /**
	   * Allows the manipulation mode to draw custom user interface elements.
	   * @param c The canvas to draw on.
	   */
	  public void draw(Canvas c) {
		}
 
    /**
     * Action to take when a finger is lifted.
     * @param event Event info.
     */
	  public void onUp(MotionEvent event) {
	  }
}