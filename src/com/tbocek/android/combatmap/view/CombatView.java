package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
     */
    private CombatViewInteractionMode mInteractionMode;

    /**
     * The color to use when creating a new line.
     */
    private int mNewLineColor = Color.BLACK;

    /**
     * The stroke width to use when creating a new line.
     */
    private int mNewLineStrokeWidth = 2;

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
    private boolean snapToGrid = true;

    /**
     * Whether to draw the annotation layer.
     */
    private boolean shouldDrawAnnotations = false;

    public interface CombatViewEventListener {
        public void onOpenTokenMenu(BaseToken t);
    };

    CombatViewEventListener mCombatViewEventListener;

    /**
     * Constructor.s
     * @param context The context to create this view in.\
     */
    public CombatView(final Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);

        this.setTokenManipulationMode();
        this.setOnDragListener(mOnDrag);
    }

    /**
     * Sets the interaction mode to simple zooming and panning.
     */
    public void setZoomPanMode() {
        setInteractionMode(new ZoomPanInteractionMode(this));
    }

    /**
     * Sets the interaction mode to dragging tokens; this will zoom and pan
     * when not on a token.  Note that annotations should always draw in this
     * mode.
     */
    public void setTokenManipulationMode() {
        setInteractionMode(new TokenManipulationInteractionMode(this));
        shouldDrawAnnotations = true;
    }

    /**
     * Sets the interaction mode to drawing lines.
     */
    public void setDrawMode() {
        setInteractionMode(new FingerDrawInteractionMode(this));
    }

    /**
     * Sets the interaction mode to erasing lines.
     */
    public void setEraseMode() {
        setInteractionMode(new EraserInteractionMode(this));
    }

    /**
     * Sets the interaction mode to resizing the grid independent of anything
     * already drawn.
     */
    public void setResizeGridMode() {
        setInteractionMode(new GridRepositioningInteractionMode(this));
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

    /**
     * Sets the interaction mode to the given listener.
     * TODO: Rename GestureListener to InteractionMode throughout.
     * @param mode The interaction mode to use.
     */
    private void setInteractionMode(final CombatViewInteractionMode mode) {
        gestureDetector = new GestureDetector(this.getContext(), mode);
        gestureDetector.setOnDoubleTapListener(mode);
        scaleDetector = new ScaleGestureDetector(this.getContext(), mode);
        mInteractionMode = mode;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        this.gestureDetector.onTouchEvent(ev);
        this.scaleDetector.onTouchEvent(ev);

        //If a finger was removed, optimize the lines by removing unused points.
        //TODO(tim.bocek): Only do this if we are erasing.
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            this.mInteractionMode.onUp(ev);
        }
        return true;
    }

    @Override
    public void onDraw(final Canvas canvas) {
        // White background
        getData().grid.draw(canvas, getData().transformer);
        getData().mBackgroundLines.drawAllLines(canvas, getData().transformer);
        getData().tokens.drawAllTokens(canvas, getGridSpaceTransformer());

        if (this.shouldDrawAnnotations) {
            getData().mAnnotationLines.drawAllLines(
                    canvas, getData().transformer);
        }

        this.mInteractionMode.draw(canvas);
    }

    /**
     * Gets a preview image of the map currently displayed in the view.
     * @return A bitmap containing the preview image.
     */
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

    /**
     * Returns the world space to screen space transformer used by the view.
     * @return The transformation object.
     */
    public CoordinateTransformer getTransformer() {
        return this.getData().transformer;
    }

    /**
     * Creates a new line on whatever line set is currently active, using the
     * currently set color and stroke width.
     * @return The new line.
     */
    public Line createLine() {
        return mActiveLines.createLine(
                this.mNewLineColor, this.mNewLineStrokeWidth);
    }

    /**
     * Places a token on the screen, at a location chosen by the view.
     * @param t The token to place.
     */
    public void placeToken(final BaseToken t) {
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

    /**
     * Removes all data.
     * TODO: still used?
     */
    public void clearAll() {
        this.getData().mBackgroundLines.clear();
        this.getData().mAnnotationLines.clear();
        this.getData().tokens.clear();
        invalidate();
    }

    /**
     * Removes all erased points from the currently active set of lines.
     */
    public void optimizeActiveLines() {
        mActiveLines.optimize();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu) {
        super.onCreateContextMenu(menu);
        mInteractionMode.onCreateContextMenu(menu);
    }

    /**
     * Forwards the context item selection event to the current interaction
     * mode.
     * @param item The selected item.
     * @return True if the event was handled.
     */
    public boolean onContextItemSelected(final MenuItem item) {
        boolean ret = mInteractionMode.onContextItemSelected(item);
        if (ret) {
        	invalidate(); // Gesture listener made changes, need to redraw.
        }
        return ret;
    }

    /**
     * Drag and drop listener that allows the user to drop tokens onto the grid.
     */
    private View.OnDragListener mOnDrag = new View.OnDragListener() {
        @Override
        public boolean onDrag(final View view, final DragEvent event) {
            Log.d("DRAG", Integer.toString(event.getAction()));
            if (event.getAction() == DragEvent.ACTION_DROP) {
                BaseToken toAdd = (BaseToken) event.getLocalState();
                PointF location =
                    getGridSpaceTransformer().screenSpaceToWorldSpace(
                            new PointF(event.getX(), event.getY()));
                if (snapToGrid) {
                    location = getData().grid.getNearestSnapPoint(
                            location, toAdd.getSize());
                }
                toAdd.setLocation(location);
                getData().tokens.addToken(toAdd);
                invalidate();
                return true;
            } else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                return true;
            }
            return false;
        }
    };

    /**
     * Gets the currently active line collection.
     * @return The active lines.
     */
    public LineCollection getActiveLines() {
        return mActiveLines;
    }

    /**
     * Returns the current token collection.
     * @return The tokens.
     */
    public TokenCollection getTokens() {
        return getData().tokens;
    }

    /**
     * Gets a transformer from grid space to screen space, by composing the
     * grid to world and the world to screen transformers.
     * @return The composed transformation.
     */
    public CoordinateTransformer getGridSpaceTransformer() {
        return getData().grid.gridSpaceToScreenSpaceTransformer(
                getData().transformer);
    }

    /**
     * Sets the map data displayed.  Forces a redraw.
     * @param data The new map data.
     */
    public void setData(final MapData data) {
        mData = data;
        invalidate();
    }

    /**
     * Gets the current map data.
     * @return data The map data.
     */
    public MapData getData() {
        return mData;
    }

	/**
	 * @param shouldSnapToGrid the shouldSnapToGrid to set
	 */
	public void setShouldSnapToGrid(final boolean shouldSnapToGrid) {
		this.snapToGrid = shouldSnapToGrid;
	}

	/**
	 * @return the shouldSnapToGrid
	 */
	public boolean shouldSnapToGrid() {
		return snapToGrid;
	}

	/**
	 * @param newLineStrokeWidth the newLineStrokeWidth to set
	 */
	public void setNewLineStrokeWidth(final int newLineStrokeWidth) {
		this.mNewLineStrokeWidth = newLineStrokeWidth;
	}

	/**
	 * @return the newLineStrokeWidth
	 */
	public int getNewLineStrokeWidth() {
		return mNewLineStrokeWidth;
	}

	/**
	 * @param newLineColor the newLineColor to set
	 */
	public void setNewLineColor(final int newLineColor) {
		this.mNewLineColor = newLineColor;
	}

	/**
	 * @return the newLineColor
	 */
	public int getNewLineColor() {
		return mNewLineColor;
	}

}
