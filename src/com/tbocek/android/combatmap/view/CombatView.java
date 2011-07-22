package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.Line;
import com.tbocek.android.combatmap.graphicscore.LineCollection;
import com.tbocek.android.combatmap.graphicscore.MapData;
import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.TokenCollection;

/**
 * This view is the main canvas on which the map and combat tokens are drawn
 * and manipulated.
 * @author Tim Bocek
 *
 */
public final class CombatView extends View {
    Paint paint = new Paint();

    /**
     * Detector object to detect regular gestures.
     */
    private GestureDetector gestureDetector;
    
    /**
     * Detector object to detect pinch zoom.
     */
    private ScaleGestureDetector scaleDetector;
    
    /**
     * Interaction mode, defining how the view should currently respond to
     * user input.
     * TODO: refactor/rename.
     */
    private CombatViewInteractionMode mGestureListener;

    /**
     * The color to use when creating a new line.
     */
    public int newLineColor = Color.BLACK;
    
    /**
     * The stroke width to use when creating a new line.
     */
    public int newLineStrokeWidth = 2;

    /**
     * The current map.
     */
    private MapData mData;

    /**
     * Reference to the collection of lines that are actively being drawn.
     */
    private LineCollection mActiveLines;
    
    /**
     * Whether tokens being moved should snap to the grid.
     */
    public boolean shouldSnapToGrid = true;
    
    /**
     * Whether to draw the annotation layer.
     */
    boolean shouldDrawAnnotations = false;

    public interface CombatViewEventListener {
        public void onOpenTokenMenu(BaseToken t);
    };

    CombatViewEventListener mCombatViewEventListener;

    public CombatView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint.setAntiAlias(true);
        this.setTokenManipulationMode();
        this.setOnDragListener(mOnDrag);
    }

    /**
     * Sets the interaction mode to simple zooming and panning.
     */
    public void setZoomPanMode() {
        setGestureListener(new ZoomPanInteractionMode(this));
    }

    /**
     * Sets the interaction mode to dragging tokens; this will zoom and pan
     * when not on a token.  Note that annotations should always draw in this
     * mode.
     */
    public void setTokenManipulationMode() {
        setGestureListener(new TokenManipulationInteractionMode(this));
        shouldDrawAnnotations = true;
    }

    /**
     * Sets the interaction mode to drawing lines.
     */
    public void setDrawMode() {
        setGestureListener(new FingerDrawInteractionMode(this));
    }
    
    /**
     * Sets the interaction mode to erasing lines.
     */
    public void setEraseMode() {
        setGestureListener(new EraserInteractionMode(this));
    }

    /**
     * Sets the interaction mode to resizing the grid independent of anything
     * already drawn.
     */
    public void setResizeGridMode() {
        setGestureListener(new GridRepositioningInteractionMode(this));
    }

    /**
     * Sets the background layer as the active layer, so that any draw commands
     * will draw on the background.
     */
    public void useBackgroundLayer() {
        mActiveLines = getData().mBackgroundLines;
        shouldDrawAnnotations = false;
    }

    /**
     * Sets the annotation layer as the active layer, so that any draw commands
     * will draw on the annotations.
     */
    public void useAnnotationLayer() {
        mActiveLines = getData().mAnnotationLines;
        shouldDrawAnnotations = true;
    }

  //TODO: needed?
    public void setEraseAnnotationMode() {
        setGestureListener(new EraserInteractionMode(this));
        useAnnotationLayer();
        shouldDrawAnnotations = true;
    }

    /**
     * Sets the interaction mode to the given listener.
     * TODO: Rename GestureListener to InteractionMode throughout.
     * @param listener The interaction mode to use.
     */
    private void setGestureListener(CombatViewInteractionMode listener) {
        gestureDetector = new GestureDetector(this.getContext(), listener);
        gestureDetector.setOnDoubleTapListener(listener);
        scaleDetector = new ScaleGestureDetector(this.getContext(), listener);
        mGestureListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        this.gestureDetector.onTouchEvent(ev);
        this.scaleDetector.onTouchEvent(ev);

        //If a finger was removed, optimize the lines by removing unused points.
        //TODO(tim.bocek): Only do this if we are erasing.
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            this.mGestureListener.onUp(ev);
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        // White background
        getData().grid.draw(canvas, getData().transformer);
        getData().mBackgroundLines.drawAllLines(canvas, getData().transformer);
        getData().tokens.drawAllTokens(canvas, getGridSpaceTransformer());

        if (this.shouldDrawAnnotations) {
            getData().mAnnotationLines.drawAllLines(
                    canvas, getData().transformer);
        }

        this.mGestureListener.draw(canvas);
    }

    public Bitmap getPreview() {
        Bitmap bitmap = Bitmap.createBitmap(
                this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        getData().grid.drawBackground(canvas);
        getData().mBackgroundLines.drawAllLines(canvas, getData().transformer);
        getData().tokens.drawAllTokens(canvas, getGridSpaceTransformer());
        getData().mAnnotationLines.drawAllLines(canvas, getData().transformer);

        return bitmap;
    }

    public CoordinateTransformer getTransformer() {
        return this.getData().transformer;
    }

    public Line createLine() {
        return mActiveLines.createLine(
                this.newLineColor, this.newLineStrokeWidth);
    }

    public void placeToken(BaseToken t) {
        PointF attemptedLocationScreenSpace =
            new PointF(this.getWidth() / 2, this.getHeight() / 2);
        //TODO: This smells really bad.
        PointF attemptedLocationGridSpace =
            this.getData().grid.gridSpaceToScreenSpaceTransformer(
                    this.getData().transformer)
                    .screenSpaceToWorldSpace(attemptedLocationScreenSpace);
        getData().tokens.placeTokenNearby(
                t, attemptedLocationGridSpace, getData().grid);
        this.getData().tokens.addToken(t);
        invalidate();
    }

    public void clearAll() {
        this.getData().mBackgroundLines.clear();
        this.getData().mAnnotationLines.clear();
        this.getData().tokens.clear();
        invalidate();
    }

    public void optimizeActiveLines() {
        mActiveLines.optimize();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        mGestureListener.onCreateContextMenu(menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        boolean ret = mGestureListener.onContextItemSelected(item);
        if (ret) invalidate(); // Gesture listener made changes, need to redraw.
        return ret;
    }

    public View.OnDragListener mOnDrag = new View.OnDragListener() {
        @Override
        public boolean onDrag(View view, DragEvent event) {
            Log.d("DRAG", Integer.toString(event.getAction()));
            if (event.getAction() == DragEvent.ACTION_DROP) {
                BaseToken toAdd = (BaseToken) event.getLocalState();
                PointF location =
                    getGridSpaceTransformer().screenSpaceToWorldSpace(
                            new PointF(event.getX(), event.getY()));
                if (shouldSnapToGrid) {
                    location = getData().grid.getNearestSnapPoint(
                            location, toAdd.getSize());
                }
                toAdd.setLocation(location);
                getData().tokens.addToken(toAdd);
                invalidate();
                return true;
            }
            else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                return true;
            }
            return false;
        }
    };

    public LineCollection getActiveLines() {
        return mActiveLines;
    }

    public TokenCollection getTokens() {
        return getData().tokens;
    }

    public CoordinateTransformer getGridSpaceTransformer() {
        return getData().grid.gridSpaceToScreenSpaceTransformer(
                getData().transformer);
    }

    public void setData(MapData data) {
        mData = data;
        invalidate();
    }

    public MapData getData() {
        return mData;
    }

}
