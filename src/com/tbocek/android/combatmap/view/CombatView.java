package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.LineCollection;
import com.tbocek.android.combatmap.graphicscore.MapData;
import com.tbocek.android.combatmap.graphicscore.PointF;
import com.tbocek.android.combatmap.graphicscore.Shape;
import com.tbocek.android.combatmap.graphicscore.TokenCollection;

/**
 * This view is the main canvas on which the map and combat tokens are drawn and
 * manipulated.
 *
 * @author Tim Bocek
 *
 */
public final class CombatView extends SurfaceView {
	/**
	 * Detector object to detect regular gestures.
	 */
	private GestureDetector gestureDetector;

	/**
	 * Detector object to detect pinch zoom.
	 */
	private ScaleGestureDetector scaleDetector;

	/**
	 * Interaction mode, defining how the view should currently respond to user
	 * input.
	 */
	private CombatViewInteractionMode mInteractionMode;

	/**
	 * The color to use when creating a new line.
	 */
	private int mNewLineColor = Color.BLACK;

	/**
	 * The stroke width to use when creating a new line.
	 */
	private float mNewLineStrokeWidth = .2f;

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

	/**
	 * Whether to draw private GM notes.
	 */
	private boolean shouldDrawGmNotes = false;

	/**
	 * Whether tokens should be drawn as manipulatable.
	 */
	private boolean areTokensManipulatable = true;

	/**
	 * Buffer to draw on to.
	 */
	private Bitmap buffer;

	boolean surfaceReady = false;

	SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			surfaceReady = false;

		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			surfaceReady = true;
			refreshMap();
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			refreshMap();
		}
	};

	/**
	 * Options for what to do with the fog of war.
	 *
	 * @author Tim
	 *
	 */
	public enum FogOfWarMode {
		/**
		 * Ignore the fog of war.
		 */
		NOTHING,

		/**
		 * Draw the fog of war as an overlay.
		 */
		DRAW,

		/**
		 * Use the fog of war to clip the background.
		 */
		CLIP
	}

	/**
	 * What to do with the fog of war when drawing.
	 */
	private FogOfWarMode mFogOfWarMode;

	/**
	 * The style that new lines should have.
	 * @author Tim
	 *
	 */
	public enum NewLineStyle {
		/**
		 * Draw freehand lines.
		 */
		FREEHAND,

		/**
		 * Draw straight lines.
		 */
		STRAIGHT,

		/**
		 * Draw a circle.
		 */
		CIRCLE,

		/**
		 * Draw text.
		 */
		TEXT
	}

	/**
	 * The style that new lines should have.
	 */
	private NewLineStyle mNewLineStyle = NewLineStyle.FREEHAND;


	/**
	 * Constructor.
	 *
	 * @param context The context to create this view in.
	 */
	public CombatView(final Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);

		this.setTokenManipulationMode();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mOnDrag = new View.OnDragListener() {
				@Override
				public boolean onDrag(final View view, final DragEvent event) {
					Log.d("DRAG", Integer.toString(event.getAction()));
					if (event.getAction() == DragEvent.ACTION_DROP) {
						BaseToken toAdd = (BaseToken) event.getLocalState();
						PointF location = getGridSpaceTransformer()
								.screenSpaceToWorldSpace(
										new PointF(event.getX(), event.getY()));
						if (snapToGrid) {
							location = getData().getGrid().getNearestSnapPoint(
									location, toAdd.getSize());
						}
						toAdd.setLocation(location);
						getData().getTokens().addToken(toAdd);
						refreshMap();
						return true;
					} else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
						return true;
					}
					return false;
				}
			};
			this.setOnDragListener(mOnDrag);
		}

		getHolder().addCallback(this.surfaceHolderCallback);
		// setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	/**
	 * Sets the interaction mode to simple zooming and panning.
	 */
	public void setZoomPanMode() {
		setInteractionMode(new ZoomPanInteractionMode(this));
	}

	/**
	 * Sets the interaction mode to dragging tokens; this will zoom and pan when
	 * not on a token. Note that annotations should always draw in this mode.
	 */
	public void setTokenManipulationMode() {
		setInteractionMode(new TokenManipulationInteractionMode(this));
		shouldDrawAnnotations = true;
		shouldDrawGmNotes = false;
	}

	/**
	 * Sets the interaction mode to drawing lines.
	 */
	public void setDrawMode() {
		setInteractionMode(new FingerDrawInteractionMode(this));
	}

	public void setTextMode() {
		setInteractionMode(new DrawTextInteractionMode(this));
	}

	/**
	 * Sets the interaction mode to drawing fog of war regions.
	 */
	public void setFogOfWarDrawMode() {
		setInteractionMode(new MaskDrawInteractionMode(this));
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
	 * Sets the interaction mode to deleting the tapped region.
	 */
	public void setDeleteMode() {
		setInteractionMode(new DeleteRegionInteractionMode(this));
	}

	/**
	 * Sets the background layer as the active layer, so that any draw commands
	 * will draw on the background.
	 */
	public void useBackgroundLayer() {
		mActiveLines = getData().getBackgroundLines();
		shouldDrawAnnotations = false;
		shouldDrawGmNotes = false;
	}

	/**
	 * Sets the annotation layer as the active layer, so that any draw commands
	 * will draw on the annotations.
	 */
	public void useAnnotationLayer() {
		mActiveLines = getData().getAnnotationLines();
		shouldDrawAnnotations = true;
		shouldDrawGmNotes = false;
	}

	/**
	 * Sets the gm note layer as the active layer, so that any draw commands
	 * will draw on the annotations.
	 */
	public void useGmNotesLayer() {
		mActiveLines = getData().getGmNoteLines();
		shouldDrawAnnotations = false;
		shouldDrawGmNotes = true;
	}


	/**
	 * Sets the interaction mode to the given listener.
	 *
	 * @param mode
	 *            The interaction mode to use.
	 */
	private void setInteractionMode(final CombatViewInteractionMode mode) {
		if (mInteractionMode != null) {
			mInteractionMode.onEndMode();
		}
		
		gestureDetector = new GestureDetector(this.getContext(), mode);
		gestureDetector.setOnDoubleTapListener(mode);
		scaleDetector = new ScaleGestureDetector(this.getContext(), mode);
		mInteractionMode = mode;
		
		if (mInteractionMode != null) {
			mInteractionMode.onStartMode();
		}
		
		this.refreshMap();
	}

	/**
	 * Sets what to do with the fog of war layer.
	 *
	 * @param mode The fog of war mode to use.
	 */
	public void setFogOfWarMode(final FogOfWarMode mode) {
		mFogOfWarMode = mode;
		refreshMap();
	}

	/**
	 *
	 * @return The fog of war mode.
	 */
	public FogOfWarMode getFogOfWarMode() {
		return mFogOfWarMode;
	}

	/**
	 * Sets whether tokens are manipulatable.
	 * @param manip Value to set.
	 */
	public void setAreTokensManipulatable(boolean manip) {
		areTokensManipulatable = manip;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		this.gestureDetector.onTouchEvent(ev);
		this.scaleDetector.onTouchEvent(ev);

		// If a finger was removed, optimize the lines by removing unused
		// points.
		// TODO(tim.bocek): Only do this if we are erasing.
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			this.mInteractionMode.onUp(ev);
		}
		return true;
	}

	/**
	 * Redraws the contents of the map.
	 */
	public void refreshMap() {
		if (!surfaceReady) return;
		SurfaceHolder holder = getHolder();
		Canvas canvas = holder.lockCanvas();
		if (canvas != null) {
			drawOnCanvas(canvas);
			holder.unlockCanvasAndPost(canvas);
		}
	}

	/**
	 * Draws the contents of the view to the given canvas.
	 *
	 * @param canvas
	 *            The canvas to draw on.
	 */
	private void drawOnCanvas(final Canvas canvas) {
		getData().getGrid().drawBackground(canvas);

		canvas.save();
		getData().transformer.setMatrix(canvas);
		if (mFogOfWarMode == FogOfWarMode.CLIP) {
			getData().getFogOfWar().clipFogOfWar(canvas);
		}
		getData().getBackgroundLines().drawAllLinesBelowGrid(canvas);
		canvas.restore();

		getData().getGrid().drawGrid(canvas, getData().transformer);

		canvas.save();
		getData().transformer.setMatrix(canvas);
		if (mFogOfWarMode == FogOfWarMode.CLIP) {
			getData().getFogOfWar().clipFogOfWar(canvas);
		}
		getData().getBackgroundLines().drawAllLinesAboveGrid(canvas);
		if (mFogOfWarMode == FogOfWarMode.DRAW) {
			getData().getFogOfWar().drawFogOfWar(canvas);
		}
		canvas.restore();

		canvas.save();
		getData().transformer.setMatrix(canvas);
		if (this.shouldDrawGmNotes) {
			getData().getGmNoteLines().drawAllLines(canvas);
		}
		if (this.shouldDrawAnnotations) {

			getData().getAnnotationLines().drawAllLines(canvas);
		}
		canvas.restore();

		canvas.save();
		if (mFogOfWarMode == FogOfWarMode.CLIP) {
			getData().transformer.setMatrix(canvas);
			getData().getFogOfWar().clipFogOfWar(canvas);
			getData().transformer.setInverseMatrix(canvas);
		}

		getData().getTokens().drawAllTokens(canvas, getGridSpaceTransformer(),
				getData().getGrid().isDark(), areTokensManipulatable);
		canvas.restore();

		this.mInteractionMode.draw(canvas);
	}

	/**
	 * Gets a preview image of the map currently displayed in the view.
	 *
	 * @return A bitmap containing the preview image.
	 */
	public Bitmap getPreview() {
		if (this.getWidth() == 0 || this.getHeight() == 0) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		getData().getGrid().drawBackground(canvas);

		canvas.save();
		getData().transformer.setMatrix(canvas);
		getData().getBackgroundLines().drawAllLines(canvas);
		getData().getAnnotationLines().drawAllLines(canvas);
		canvas.restore();

		getData().getTokens().drawAllTokens(canvas, getGridSpaceTransformer(),
				getData().getGrid().isDark(), true);

		return bitmap;
	}

	/**
	 * Returns the world space to screen space transformer used by the view.
	 *
	 * @return The transformation object.
	 */
	public CoordinateTransformer getWorldSpaceTransformer() {
		return this.getData().transformer;
	}

	/**
	 * Creates a new line on whatever line set is currently active, using the
	 * currently set color and stroke width.
	 *
	 * @return The new line.
	 */
	public Shape createLine() {
		return createLine(mActiveLines);
	}

	protected Shape createLine(LineCollection lines) {
		switch(this.mNewLineStyle) {
		case FREEHAND:
			return lines.createFreehandLine(this.mNewLineColor,
					this.mNewLineStrokeWidth);
		case STRAIGHT:
			return lines.createStraightLine(this.mNewLineColor,
					this.mNewLineStrokeWidth);
		case CIRCLE:
			return lines.createCircle(this.mNewLineColor,
					this.mNewLineStrokeWidth);
		default:
			throw new IllegalArgumentException("Invalid new line type.");
		}
	}

	/**
	 * Creates a new region in the fog of war.
	 * @return The new region.
	 */
	public Shape createFogOfWarRegion() {
		return createLine(getData().getFogOfWar());
	}

	/**
	 * Places a token on the screen, at a location chosen by the view.
	 *
	 * @param t The token to place.
	 */
	public void placeToken(final BaseToken t) {
		PointF attemptedLocationScreenSpace = new PointF(this.getWidth() / 2,
				this.getHeight() / 2);
		// TODO: This smells really bad.
		PointF attemptedLocationGridSpace = this.getGridSpaceTransformer()
				.screenSpaceToWorldSpace(attemptedLocationScreenSpace);

		getData().getTokens().placeTokenNearby(t, attemptedLocationGridSpace,
				getData().getGrid());
		this.getData().getTokens().addToken(t);
		refreshMap();
	}

	/**
	 * Removes all data. TODO: still used?
	 */
	public void clearAll() {
		this.getData().getBackgroundLines().clear();
		this.getData().getAnnotationLines().clear();
		this.getData().getTokens().clear();
		refreshMap();
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
	 *
	 * @param item
	 *            The selected item.
	 * @return True if the event was handled.
	 */
	public boolean onContextItemSelected(final MenuItem item) {
		boolean ret = mInteractionMode.onContextItemSelected(item);
		if (ret) {
			refreshMap(); // Gesture listener made changes, need to redraw.
		}
		return ret;
	}

	/**
	 * Drag and drop listener that allows the user to drop tokens onto the grid.
	 */
	private View.OnDragListener mOnDrag;

	/**
	 * Gets the currently active line collection.
	 *
	 * @return The active lines.
	 */
	public LineCollection getActiveLines() {
		return mActiveLines;
	}

	/**
	 * Returns the current token collection.
	 *
	 * @return The tokens.
	 */
	public TokenCollection getTokens() {
		return getData().getTokens();
	}

	/**
	 * Gets a transformer from grid space to screen space, by composing the grid
	 * to world and the world to screen transformers.
	 *
	 * @return The composed transformation.
	 */
	public CoordinateTransformer getGridSpaceTransformer() {
		return getData().getGrid().gridSpaceToScreenSpaceTransformer(
				getData().transformer);
	}

	/**
	 * Sets the map data displayed. Forces a redraw.
	 *
	 * @param data
	 *            The new map data.
	 */
	public void setData(final MapData data) {

		boolean useBackgroundLines = (mData == null)
				|| this.mActiveLines == mData.getBackgroundLines();
		mData = data;
		this.mActiveLines = useBackgroundLines ? mData.getBackgroundLines()
				: mData.getAnnotationLines();
		refreshMap();
	}

	/**
	 * Gets the current map data.
	 *
	 * @return data The map data.
	 */
	public MapData getData() {
		return mData;
	}

	/**
	 * @param shouldSnapToGrid
	 *            the shouldSnapToGrid to set
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
	 * @param width
	 *            the newLineStrokeWidth to set
	 */
	public void setNewLineStrokeWidth(final float width) {
		this.mNewLineStrokeWidth = width;
	}

	/**
	 * @return the newLineStrokeWidth
	 */
	public float getNewLineStrokeWidth() {
		return mNewLineStrokeWidth;
	}

	/**
	 * @param newLineColor
	 *            the newLineColor to set
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

	/**
	 * @param newLineStyle the newLineStyle to set
	 */
	public void setNewLineStyle(NewLineStyle newLineStyle) {
		mNewLineStyle = newLineStyle;
	}

	/**
	 * @return the newLineStyle
	 */
	public NewLineStyle getNewLineStyle() {
		return mNewLineStyle;
	}

	
	
	public interface NewTextEntryListener {
		void requestNewTextEntry(PointF newTextLocationWorldSpace);
	}
	public NewTextEntryListener newTextEntryListener;
	
	public void requestNewTextEntry(PointF newTextLocationWorldSpace) {
		if (newTextEntryListener != null) {
			newTextEntryListener.requestNewTextEntry(newTextLocationWorldSpace);
		}
	}
	
	public void createNewText(PointF newTextLocationWorldSpace, String text, float size) {
		//Compute the text size as being one grid cell large.
		float textSize = getData().getGrid().gridSpaceToWorldSpaceTransformer()
				   .worldSpaceToScreenSpace(size);
		mActiveLines.createText(text, textSize,
				mNewLineColor, Float.POSITIVE_INFINITY, 
				newTextLocationWorldSpace, this.getWorldSpaceTransformer());
		refreshMap();
	}
}