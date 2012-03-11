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

import com.tbocek.android.combatmap.model.LineCollection;
import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.MapDrawer;
import com.tbocek.android.combatmap.model.MapDrawer.FogOfWarMode;
import com.tbocek.android.combatmap.model.MultiSelectManager;
import com.tbocek.android.combatmap.model.TokenCollection;
import com.tbocek.android.combatmap.model.UndoRedoTarget;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Shape;
import com.tbocek.android.combatmap.model.primitives.Text;
import com.tbocek.android.combatmap.view.interaction.BackgroundImageInteractionMode;
import com.tbocek.android.combatmap.view.interaction.CombatViewInteractionMode;
import com.tbocek.android.combatmap.view.interaction.DrawTextInteractionMode;
import com.tbocek.android.combatmap.view.interaction.EraserInteractionMode;
import com.tbocek.android.combatmap.view.interaction.FingerDrawInteractionMode;
import com.tbocek.android.combatmap.view.interaction.GridRepositioningInteractionMode;
import com.tbocek.android.combatmap.view.interaction.MaskDrawInteractionMode;
import com.tbocek.android.combatmap.view.interaction.TokenManipulationInteractionMode;
import com.tbocek.android.combatmap.view.interaction.TokenMultiSelectInteractionMode;
import com.tbocek.android.combatmap.view.interaction.ZoomPanInteractionMode;

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
	private GestureDetector mGestureDetector;

	/**
	 * Detector object to detect pinch zoom.
	 */
	private ScaleGestureDetector mScaleDetector;

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
	private float mNewLineStrokeWidth;

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
	private boolean mSnapToGrid;

	/**
	 * Whether to draw the annotation layer.
	 */
	private boolean mShouldDrawAnnotations;

	/**
	 * Whether to draw private GM notes.
	 */
	private boolean mShouldDrawGmNotes;

	/**
	 * Whether tokens should be drawn as manipulatable.
	 */
	private boolean mAreTokensManipulatable = true;

	/**
	 * Whether the surface is ready to draw.
	 */
	private boolean mSurfaceReady = false;
	
	/**
	 * Drag and drop listener that allows the user to drop tokens onto the grid.
	 */
	private View.OnDragListener mOnDrag;
	
	/**
	 * The current map data object that undo/redo actions will affect.
	 */
	private UndoRedoTarget mUndoRedoTarget;
	
	/**
	 * Callback that the parent activity registers to listen to requests for
	 * new/edited text objects.  This indirection is needed because this
	 * operation requires a dialog, which the activity needs to open.
	 */
	private NewTextEntryListener mNewTextEntryListener;
	
	/**
	 * Listener to publish refresh requests to.
	 */
	private OnRefreshListener mOnRefreshListener;

	/**
	 * Object to manage a selection of multiple tokens.
	 */
    private MultiSelectManager mTokenSelection = new MultiSelectManager();

	/**
	 * Callback for the Android graphics surface management system.
	 */
	private SurfaceHolder.Callback mSurfaceHolderCallback 
			= new SurfaceHolder.Callback() {
		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			mSurfaceReady = false;

		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			mSurfaceReady = true;
			refreshMap();
		}

		@Override
		public void surfaceChanged(
				SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			refreshMap();
		}
	};

	/**
	 * What to do with the fog of war when drawing.
	 */
	private MapDrawer.FogOfWarMode mFogOfWarMode;

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
		TEXT, 
		
		/**
		 * Draw rectangle.
		 */
		RECTANGLE
	}

	/**
	 * The style that new lines should have.
	 */
	private NewLineStyle mNewLineStyle = NewLineStyle.FREEHAND;

	/**
	 * Whether tokens snap to the intersections of grid lines rather 
	 * than the spaces between them.
	 */
	private boolean mTokensSnapToIntersections;


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
						if (mSnapToGrid) {
							location = getData().getGrid().getNearestSnapPoint(
									location, toAdd.getSize());
						}
						toAdd.setLocation(location);
						getData().getTokens().addToken(toAdd);
						refreshMap();
						return true;
					} else if (event.getAction() 
							== DragEvent.ACTION_DRAG_STARTED) {
						return true;
					}
					return false;
				}
			};
			this.setOnDragListener(mOnDrag);
		}

		getHolder().addCallback(this.mSurfaceHolderCallback);
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
		mShouldDrawAnnotations = true;
		mShouldDrawGmNotes = false;
		if (mData != null) {
			mUndoRedoTarget = mData.getTokens();
		}
	}
	

	/**
	 * Sets the interaction mode to selecting multiple tokens.
	 */
	public void setMultiTokenMode() {
		setInteractionMode(new TokenMultiSelectInteractionMode(this));
	}

	/**
	 * Sets the interaction mode to drawing lines.
	 */
	public void setDrawMode() {
		setInteractionMode(new FingerDrawInteractionMode(this));
	}

	/**
	 * Sets the interaction mode to creating text objects.
	 */
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
	 * Sets the interaction mode to creating and manipulating background
	 * images.
	 */
	public void setBackgroundImageMode() {
		setInteractionMode(new BackgroundImageInteractionMode(this));
	}

	/**
	 * Sets the background layer as the active layer, so that any draw commands
	 * will draw on the background.
	 */
	public void useBackgroundLayer() {
		mActiveLines = getData().getBackgroundLines();
		mShouldDrawAnnotations = false;
		mShouldDrawGmNotes = false;
		mUndoRedoTarget = mActiveLines;
	}

	/**
	 * Sets the annotation layer as the active layer, so that any draw commands
	 * will draw on the annotations.
	 */
	public void useAnnotationLayer() {
		mActiveLines = getData().getAnnotationLines();
		mShouldDrawAnnotations = true;
		mShouldDrawGmNotes = false;
		mUndoRedoTarget = mActiveLines;
	}

	/**
	 * Sets the gm note layer as the active layer, so that any draw commands
	 * will draw on the annotations.
	 */
	public void useGmNotesLayer() {
		mActiveLines = getData().getGmNoteLines();
		mShouldDrawAnnotations = false;
		mShouldDrawGmNotes = true;
		mUndoRedoTarget = mActiveLines;
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
		
		mGestureDetector = new GestureDetector(this.getContext(), mode);
		mGestureDetector.setOnDoubleTapListener(mode);
		mScaleDetector = new ScaleGestureDetector(this.getContext(), mode);
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
		mAreTokensManipulatable = manip;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			this.mInteractionMode.addFinger();
		}
		
		this.mGestureDetector.onTouchEvent(ev);
		this.mScaleDetector.onTouchEvent(ev);

		// If a finger was removed, optimize the lines by removing unused
		// points.
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			this.mInteractionMode.removeFinger();
			this.mInteractionMode.onUp(ev);
		}
		return true;
	}

	/**
	 * Redraws the contents of the map.
	 */
	public void refreshMap() {
		if (!mSurfaceReady) {
			return;
		}

		SurfaceHolder holder = getHolder();
		Canvas canvas = holder.lockCanvas();
		if (canvas != null) {
			drawOnCanvas(canvas);
			holder.unlockCanvasAndPost(canvas);
		}
		
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
	}

	/**
	 * Draws the contents of the view to the given canvas.
	 *
	 * @param canvas
	 *            The canvas to draw on.
	 */
	private void drawOnCanvas(final Canvas canvas) {
		new MapDrawer()
			.drawGridLines(true)
			.drawGmNotes(this.mShouldDrawGmNotes)
			.drawTokens(true)
			.areTokensManipulable(mAreTokensManipulatable)
			.drawAnnotations(this.mShouldDrawAnnotations)
			.gmNotesFogOfWar(this.mActiveLines == getData().getGmNoteLines()
							 ? FogOfWarMode.DRAW : FogOfWarMode.CLIP)
			.backgroundFogOfWar(mFogOfWarMode)
			.draw(canvas, getData());

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

		new MapDrawer()
			.drawGridLines(false)
			.drawGmNotes(false)
			.drawTokens(true)
			.areTokensManipulable(true)
			.drawAnnotations(false)
			.gmNotesFogOfWar(FogOfWarMode.NOTHING)
			.backgroundFogOfWar(mFogOfWarMode)
			.draw(canvas, getData());

		return bitmap;
	}

	/**
	 * Returns the world space to screen space transformer used by the view.
	 *
	 * @return The transformation object.
	 */
	public CoordinateTransformer getWorldSpaceTransformer() {
		return this.getData().getWorldSpaceTransformer();
	}

	/**
	 * Creates a new line on whatever line set is currently active, using the
	 * currently set color, stroke width, and type.
	 *
	 * @return The new line.
	 */
	public Shape createLine() {
		return createLine(mActiveLines);
	}

	/**
	 * Creates a line in the given line collection with the currently set
	 * color, stroke width, and type.
	 * @param lines The line collection to create the line in.
	 * @return The created line.
	 */
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
		case RECTANGLE:
			return lines.createRectangle(this.mNewLineColor,
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
		return createLine(getActiveFogOfWar());
	}

	/**
	 * Places a token on the screen, at a location chosen by the view.
	 *
	 * @param t The token to place.
	 */
	public void placeToken(final BaseToken t) {
		PointF attemptedLocationScreenSpace = new PointF(this.getWidth() / 2.0f,
				this.getHeight() / 2.0f);
		PointF attemptedLocationGridSpace =
				this.getGridSpaceTransformer().screenSpaceToWorldSpace(
						attemptedLocationScreenSpace);

		getData().getTokens().placeTokenNearby(t, attemptedLocationGridSpace,
				getData().getGrid(), this.mTokensSnapToIntersections);
		this.getData().getTokens().addToken(t);
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
				getData().getWorldSpaceTransformer());
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
		this.mSnapToGrid = shouldSnapToGrid;
	}

	/**
	 * @return the shouldSnapToGrid
	 */
	public boolean shouldSnapToGrid() {
		return mSnapToGrid;
	}
	
	/**
	 * Sets whether tokens snap to the intersections of grid lines rather than
	 * the spaces between them.
	 * @param shouldSnap True if should snap to intersections.
	 */
	public void setTokensSnapToIntersections(boolean shouldSnap) {
		mTokensSnapToIntersections = shouldSnap;
		
	}
	
	/**
	 * @return Whether tokens snap to the intersections of grid lines rather 
	 * than the spaces between them.
	 */
	public boolean tokensSnapToIntersections() {
		return mTokensSnapToIntersections;
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
	
	/**
	 * @return The multi-select manager used to select multiple tokens in this
	 * 		view.
	 */
    public MultiSelectManager getMultiSelect() {
    	return mTokenSelection;
    }

	
	/**
	 * Callback interface for the activity to listen for requests to create
	 * or edit text objects, since these actions require the activity to open
	 * a dialog box.
	 * @author Tim
	 *
	 */
	public interface NewTextEntryListener {
		/**
		 * Called when a new text object is created.
		 * @param newTextLocationWorldSpace Location to place the new text
		 * 		object in world space.
		 */
		void requestNewTextEntry(PointF newTextLocationWorldSpace);

		/**
		 * Called when an edit is requested on an existing text object.
		 * @param t The text object to edit.
		 */
		void requestEditTextObject(Text t);
	}

	/**
	 * Forwards a request to create a text object to the parent activity.
	 * @param newTextLocationWorldSpace Location to place the new text object 
	 *      in world space.
	 */
	public void requestNewTextEntry(PointF newTextLocationWorldSpace) {
		if (mNewTextEntryListener != null) {
			mNewTextEntryListener.requestNewTextEntry(
					newTextLocationWorldSpace);
		}
	}
	
	/**
	 * Creates a new text object with the given location, text, and font size.
	 * This is called by the parent activity after being alerted to the request
	 * for a new text object and after the resulting dialog is accepted.
	 * @param newTextLocationWorldSpace Location to place the new text object
	 * 		at.
	 * @param text Contents of the new text object.
	 * @param size Font size of the new text object.
	 */
	public void createNewText(
			PointF newTextLocationWorldSpace, String text, float size) {
		//Compute the text size as being one grid cell large.
		float textSize = getData().getGrid().gridSpaceToWorldSpaceTransformer()
				   .worldSpaceToScreenSpace(size);
		mActiveLines.createText(text, textSize,
				mNewLineColor, Float.POSITIVE_INFINITY, 
				newTextLocationWorldSpace, this.getWorldSpaceTransformer());
		refreshMap();
	}

	/**
	 * Forwards a request to edit a text object to the parent activity.
	 * @param t The text object to edit.
	 */
	public void requestEditTextObject(Text t) {
		mNewTextEntryListener.requestEditTextObject(t);
	}


	/**
	 * @return The fog of war layer associated with the current active lines.
	 */
	public LineCollection getActiveFogOfWar() {
		if (mActiveLines == getData().getBackgroundLines()) {
			return getData().getBackgroundFogOfWar();
		} else if (mActiveLines == getData().getGmNoteLines()) {
			return getData().getGmNotesFogOfWar();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return Whether one of the fog of war layers is visible.
	 */
	public boolean isAFogOfWarLayerVisible() {
		return  getFogOfWarMode() == FogOfWarMode.DRAW 
				|| this.mShouldDrawGmNotes;
	}
	
	/**
	 * 
	 * @return The current target for undo and redo operations.
	 */
	public UndoRedoTarget getUndoRedoTarget() {
		return mUndoRedoTarget;
	}
	
	/**
	 * Sets the listener for requests to change or create text objects.
	 * @param newTextEntryListener The listener to use.
	 */
	public void setNewTextEntryListener(
			NewTextEntryListener newTextEntryListener) {
		this.mNewTextEntryListener = newTextEntryListener;
	}
	
	/**
	 * Sets the listener to publish refresh requests to.
	 * @param onRefreshListener The listener to use.
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		mOnRefreshListener = onRefreshListener;
	}
	
	/**
	 * Called when this combat view is refreshed.
	 * @author Tim
	 *
	 */
	public interface OnRefreshListener {
		/**
		 * Called when this combat view is refreshed.
		 */
		void onRefresh();
	}





}