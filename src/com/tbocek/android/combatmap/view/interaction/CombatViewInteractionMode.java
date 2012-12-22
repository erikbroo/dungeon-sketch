package com.tbocek.android.combatmap.view.interaction;


import com.tbocek.android.combatmap.model.MapData;
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
    private CombatView mView;
    
    /**
     * Number of fingers currently down.
     */
    private int mFingers;
    
    
    /**
     * Constructor.
     * @param view The CombatView that this interaction mode manipulates.
     */
      public CombatViewInteractionMode(final CombatView view) {
          this.mView = view;
      }
      
      /**
       * Increments the number of fingers.
       */
      public final void addFinger() {
    	  mFingers++;
      }
      
      /**
       * Decrements the number of fingers.
       */
      public final void removeFinger() {
    	  mFingers--;
      }
    
      /**
       * @return Gets the number of fingers currently down.
       */
      protected int getNumberOfFingers() {
    	  return mFingers;
      }
      
      /**
       * Called when this interaction mode is started.
       */
      public void onStartMode() {
    	  
      }
      
      /**
       * Called when this interaction mode is stopped.
       */
      public void onEndMode() {
    	  
      }

      @Override
      public boolean onScale(final ScaleGestureDetector detector) {
        getView().getWorldSpaceTransformer().zoom(
            detector.getScaleFactor(),
            new PointF(detector.getFocusX(), detector.getFocusY()));
        getView().refreshMap();
        return true;
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

	/**
	 * @return The CombatView being manipulated.
	 */
	protected CombatView getView() {
		return mView;
	}
	
	protected MapData getData() {
		return mView.getData();
	}
}