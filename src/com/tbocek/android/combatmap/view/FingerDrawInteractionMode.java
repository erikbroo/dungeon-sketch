package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.Shape;
import com.tbocek.android.combatmap.graphicscore.Util;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

/**
 * Interaction mode that allows the user to draw a line on the CombatView.
 * The color and width of the line are selected and stored elsewhere in the
 * CombatView.
 * @author Tim
 *
 */
public class FingerDrawInteractionMode extends CombatViewInteractionMode {

    /**
     * Distance between successive points on a line in screen space.
     */
    private static final float POINT_RATE_LIMIT = 3;

    /**
     * The last x coordinate at which a point was added to the current line.
     */
    private float lastPointX;

    /**
     * The last y coordinate at which a point was added to the current line.
     */
    private float lastPointY;

    /**
     * The line that the user is actively drawing.  New points will be added to
     * this line.
     */
    private Shape currentLine;


	/**
	 * The point in world space that was long-pressed to open the menu.
	 */
	private PointF longPressPoint;

    /**
     * Constructor.
     * @param view The CombatView to manipulate.
     */
    public FingerDrawInteractionMode(final CombatView view) {
        super(view);
    }


    @Override
    public boolean onScroll(
            final MotionEvent e1, final MotionEvent e2,
            final float distanceX, final float distanceY) {
        if (currentLine == null) {
            return true;
        }

        if (shouldAddPoint(e2.getX(), e2.getY())) {
            addLinePoint(e2);
        }
        return true;
    }

    /**
     * Adds the location of the given motion event to the line.
     * @param e The motion event containing the point to add.
     */
    private void addLinePoint(final MotionEvent e) {
        PointF p = getScreenSpacePoint(e);
    	// Need to transform to world space.
	    currentLine.addPoint(
	            view.getTransformer().screenSpaceToWorldSpace(p));

        view.refreshMap(); // Redraw the screen
        lastPointX = p.x;
        lastPointY = p.y;
    }

    /**
     * Gets the draw location in screen space.  Snaps to the grid if necessary.
     * @param e The motion event to get the point from.
     * @return The point in screen space.
     */
    private PointF getScreenSpacePoint(final MotionEvent e) {
    	PointF p = new PointF(e.getX(), e.getY());
    	if (view.shouldSnapToGrid()) {
    		CoordinateTransformer transformer = view.getGridSpaceTransformer();
    		p = transformer.worldSpaceToScreenSpace(
    				view.getData().getGrid().getNearestSnapPoint(
    						transformer.screenSpaceToWorldSpace(p), 0));
    	}
    	return p;
    }

    /**
     * Returns True if the proposed point is far enough away from the previously
     * drawn point to add to the line.
     * @param newPointX X coordinate of the new point.
     * @param newPointY Y coordinate of the new point.
     * @return True if the point should be added
     */
    private boolean shouldAddPoint(
            final float newPointX, final float newPointY) {
        return Util.distance(lastPointX, lastPointY, newPointX, newPointY)
                > POINT_RATE_LIMIT;
    }

    @Override
    public boolean onDown(final MotionEvent e) {
        currentLine = createLine();
        PointF p = getScreenSpacePoint(e);
        lastPointX = p.x;
        lastPointY = p.y;
        return true;
    }

    protected Shape createLine() {
    	return view.createLine();
    }

    @Override
    public void onLongPress(final MotionEvent ev) {
    	longPressPoint = view.getTransformer().screenSpaceToWorldSpace(
    			ev.getX(), ev.getY());
        if (view.getFogOfWarMode() == CombatView.FogOfWarMode.DRAW &&
        		view.getData().getFogOfWar().isPointInRegion(longPressPoint)) {
            view.showContextMenu();
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
          if (item.getItemId() == R.id.fog_context_delete) {
        	  view.getData().getFogOfWar().deleteRegionsUnderPoint(
        			  longPressPoint);
        	  return true;
          }
          return false;
    }
}
