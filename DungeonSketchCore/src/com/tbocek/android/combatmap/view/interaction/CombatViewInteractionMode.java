package com.tbocek.android.combatmap.view.interaction;

import android.graphics.Canvas;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.view.CombatView;

/**
 * This class roots a strategy pattern hierarchy that primarily defines actions
 * to take on the CombatView given a particular gesture. These have been
 * extracted so that different interaction modes (panning, drawing, moving
 * tokens, etc) can easily and modularly be defined.
 * 
 * @author Tim Bocek
 */
public class CombatViewInteractionMode extends SimpleOnScaleGestureListener
        implements OnGestureListener, OnDoubleTapListener {
    /**
     * Number of fingers currently down.
     */
    private int mFingers;

    /**
     * The CombatView that this interaction mode manipulates.
     */
    private CombatView mView;

    /**
     * Constructor.
     * 
     * @param view
     *            The CombatView that this interaction mode manipulates.
     */
    public CombatViewInteractionMode(final CombatView view) {
        this.mView = view;
    }

    /**
     * Increments the number of fingers.
     */
    public final void addFinger() {
        this.mFingers++;
    }

    /**
     * Allows the manipulation mode to draw custom user interface elements.
     * 
     * @param c
     *            The canvas to draw on.
     */
    public void draw(final Canvas c) {
    }

    protected MapData getData() {
        return this.mView.getData();
    }

    /**
     * @return Gets the number of fingers currently down.
     */
    protected int getNumberOfFingers() {
        return this.mFingers;
    }

    /**
     * @return The CombatView being manipulated.
     */
    protected CombatView getView() {
        return this.mView;
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
    public boolean onDown(final MotionEvent event) {
        return false;
    }

    /**
     * Called when this interaction mode is stopped.
     */
    public void onEndMode() {

    }

    @Override
    public boolean onFling(final MotionEvent arg0, final MotionEvent arg1,
            final float arg2, final float arg3) {
        return false;
    }

    @Override
    public void onLongPress(final MotionEvent arg0) {
    }

    @Override
    public boolean onScale(final ScaleGestureDetector detector) {
        this.getView()
                .getWorldSpaceTransformer()
                .zoom(detector.getScaleFactor(),
                        new PointF(detector.getFocusX(), detector.getFocusY()));
        this.getView().refreshMap();
        return true;
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
    public boolean onSingleTapConfirmed(final MotionEvent arg0) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent ev) {
        return false;
    }

    /**
     * Called when this interaction mode is started.
     */
    public void onStartMode() {

    }

    /**
     * Action to take when a finger is lifted.
     * 
     * @param event
     *            Event info.
     */
    public void onUp(final MotionEvent event) {
    }

    /**
     * Decrements the number of fingers.
     */
    public final void removeFinger() {
        this.mFingers--;
    }
}