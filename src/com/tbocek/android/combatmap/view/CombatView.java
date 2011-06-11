package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
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
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;
import com.tbocek.android.combatmap.graphicscore.TokenCollection;

public class CombatView extends View {
	Paint paint = new Paint();
	
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleDetector;
	private CombatViewInteractionMode mGestureListener;
	

	
	public int newLineColor = Color.BLACK;
	public int newLineStrokeWidth = 2;
	
	public MapData mData;

	private LineCollection mActiveLines;
	
	public boolean shouldSnapToGrid = true;
	boolean shouldDrawAnnotations = false;
	
	public interface CombatViewEventListener {
		public void onOpenTokenMenu(BaseToken t);
	};
	
	CombatViewEventListener mCombatViewEventListener;
	
	public CombatView(Context context, MapData data) {
		super(context);
		mData = data;
		mActiveLines = data.mBackgroundLines;
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		paint.setAntiAlias(true);
		this.setTokenManipulationMode();
		this.setOnDragListener(mOnDrag);
	}
	
	public void setZoomPanMode() {
		setGestureListener(new ZoomPanInteractionMode(this));
	}
	
	public void setTokenManipulationMode() {
		setGestureListener(new TokenManipulationInteractionMode(this));
		shouldDrawAnnotations = true;
	}
	
	public void setDrawMode() {
		setGestureListener(new FingerDrawInteractionMode(this));
	}
	
	public void setEraseMode() {
		setGestureListener(new EraserInteractionMode(this));
	}

	public void setResizeGridMode() {
		setGestureListener(new GridRepositioningInteractionMode(this));
	}
	
	public void useBackgroundLayer() {
		mActiveLines = mData.mBackgroundLines;
		shouldDrawAnnotations = false;
	}
	
	public void useAnnotationLayer() {
		mActiveLines = mData.mAnnotationLines;
		shouldDrawAnnotations = true;
	}
	
	public void setEraseAnnotationMode() {
		setGestureListener(new EraserInteractionMode(this));
		useAnnotationLayer();
		shouldDrawAnnotations = true;
	}
	
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
		mData.grid.draw(canvas, mData.transformer);
		mData.mBackgroundLines.drawAllLines(canvas, mData.transformer);
		mData.tokens.drawAllTokens(canvas, getGridSpaceTransformer());
		
		if (this.shouldDrawAnnotations) {
			mData.mAnnotationLines.drawAllLines(canvas, mData.transformer);
		}
		
		this.mGestureListener.draw(canvas);
	}

	public CoordinateTransformer getTransformer() {
		return this.mData.transformer;
	}
	
	public Line createLine() {
		return mActiveLines.createLine(this.newLineColor, this.newLineStrokeWidth);
	}

	public void placeToken(BaseToken t) {
		PointF attemptedLocationScreenSpace = new PointF(this.getWidth() / 2, this.getHeight() / 2);
		PointF attemptedLocationGridSpace = this.mData.grid.gridSpaceToScreenSpaceTransformer(this.mData.transformer).screenSpaceToWorldSpace(attemptedLocationScreenSpace);
		mData.tokens.placeTokenNearby(t, attemptedLocationGridSpace, mData.grid);
		this.mData.tokens.addToken(t);
		invalidate();
	}

	public void clearAll() {
		this.mData.mBackgroundLines.clear();
		this.mData.mAnnotationLines.clear();
		this.mData.tokens.clear();
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
				PointF location = getGridSpaceTransformer().screenSpaceToWorldSpace(new PointF(event.getX(), event.getY()));
				if (shouldSnapToGrid) {
					location = mData.grid.getNearestSnapPoint(location, toAdd.getSize());
				}
				toAdd.setLocation(location);
				mData.tokens.addToken(toAdd);
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
		return mData.tokens;
	}

	public CoordinateTransformer getGridSpaceTransformer() {
		return mData.grid.gridSpaceToScreenSpaceTransformer(mData.transformer);
	}

}
