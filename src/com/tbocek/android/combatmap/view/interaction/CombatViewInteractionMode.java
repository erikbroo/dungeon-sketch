package com.tbocek.android.combatmap.view.interaction;


import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

import android.graphics.Canvas;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

/**
 * This class roots a strategy pattern hierarchy that primarily defines actions
 * to take on the CombatView given a particular gesture.  These have been
 * extracted so that different interaction modes (panning, drawing, moving
 * tokens, etc) can easily and modularly be defined.
 *
 * @author Tim Bocek
 */
public class CombatViewInteractionMode
		extends SimpleOnScaleGestureListener
		implements OnGestureListener, OnDoubleTapListener {
    /**
     * The CombatView that this interaction mode manipulates.
     */
    protected CombatView view;

    /**
     * Constructor.
     * @param view The CombatView that this interaction mode manipulates.
     */
      public CombatViewInteractionMode(final CombatView view) {
          this.view = view;
      }
      
      public void onStartMode() {
    	  
      }
      
      public void onEndMode() {
    	  
      }

      @Override
      public boolean onScale(final ScaleGestureDetector detector) {
        view.getWorldSpaceTransformer().zoom(
            detector.getScaleFactor(),
            new PointF(detector.getFocusX(), detector.getFocusY()));
        view.refreshMap();
        return true;
      }

    /**
     * Allows the interaction mode to specify custom context menu options.
     * @param menu The context menu to populate
     */
      public void onCreateContextMenu(final ContextMenu menu) {
      }

    /**
     * Allows the interaction mode to specify the actions taken in
     * response to context menu items that it added.
     * @param item The context menu item clicked.
     * @return Whether the event was handled.
     */
      public boolean onContextItemSelected(final MenuItem item) {
          return false;
    }

      /**
       * Allows the manipulation mode to draw custom user interface elements.
       * @param c The canvas to draw on.
       */
      public void draw(final Canvas c) {
        }

    /**
     * Action to take when a finger is lifted.
     * @param event Event info.
     */
      public void onUp(final MotionEvent event) {
      }

      @Override
      public boolean onDown(final MotionEvent event) {
          return false;
      }

      @Override
      public boolean onFling(
    		  final MotionEvent arg0, final MotionEvent arg1, final float arg2,
    		  final float arg3) {
          return false;
      }

      @Override
      public void onLongPress(final MotionEvent arg0) {
      }

      @Override
      public boolean onScroll(final MotionEvent arg0, final MotionEvent arg1,
    		  final float arg2, final float arg3) {
          return false;
      }

      @Override
      public void onShowPress(final MotionEvent arg0) {
      }

      @Override
      public boolean onSingleTapUp(final MotionEvent ev) {
          return false;
      }

      @Override
      public boolean onDoubleTap(final MotionEvent arg0) {
          return true;
      }

      @Override
      public boolean onDoubleTapEvent(final MotionEvent arg0) {
          return true;
      }

      @Override
      public boolean onSingleTapConfirmed(final MotionEvent arg0) {
          return true;
      }
}