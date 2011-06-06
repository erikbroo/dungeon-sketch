package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.tbocek.android.combatmap.graphicscore.CoordinateTransformer;
import com.tbocek.android.combatmap.graphicscore.Line;
import com.tbocek.android.combatmap.graphicscore.Token;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;

public class CombatView extends View implements OnTouchListener {
	CoordinateTransformer transformer = new CoordinateTransformer();

	Paint paint = new Paint();
	
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleDetector;
	
	private List<Line> mBackgroundLines = new ArrayList<Line>();
	private List<Line> mAnnotationLines = new ArrayList<Line>();
	private List<Line> mActiveLines = mBackgroundLines;
	public TokenCollection tokens = new TokenCollection();
	
	public int newLineColor = Color.BLACK;
	
	private GridColorScheme colorScheme = GridColorScheme.GRAPH_PAPER;
	
	boolean shouldDrawAnnotations = false;
	
	public CombatView(Context context) {
		super(context);

		setFocusable(true);
		setFocusableInTouchMode(true);
		this.setOnTouchListener(this);

		tokens.list().add(new Token(Color.BLUE));
		
		paint.setAntiAlias(true);
		this.setZoomPanMode();
	}
	
	public void setZoomPanMode() {
		setGestureListener(new ZoomPanGestureListener(this));
	}
	
	public void setTokenManipulationMode() {
		setGestureListener(new TokenManipulationListener(this));
		shouldDrawAnnotations = true;
	}
	
	public void setDrawMode() {
		setGestureListener(new FingerDrawGestureListener(this));
	}
	
	public void setEraseMode() {
		setGestureListener(new EraserGestureListener(this));
	}

	public void useBackgroundLayer() {
		mActiveLines = mBackgroundLines;
		shouldDrawAnnotations = false;
	}
	
	public void useAnnotationLayer() {
		mActiveLines = mAnnotationLines;
		shouldDrawAnnotations = true;
	}
	
	public void setEraseAnnotationMode() {
		setGestureListener(new EraserGestureListener(this));
		useAnnotationLayer();
		shouldDrawAnnotations = true;
	}
	
	private void setGestureListener(SimpleAllGestureListener listener) {
		gestureDetector = new GestureDetector(this.getContext(), listener);
		gestureDetector.setOnDoubleTapListener(listener);
		scaleDetector = new ScaleGestureDetector(this.getContext(), listener);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		this.gestureDetector.onTouchEvent(ev);
		this.scaleDetector.onTouchEvent(ev);
		
		//If a finger was removed, optimize the lines by removing unused points.
		//TODO(tim.bocek): Only do this if we are erasing.
		if (ev.getAction() == MotionEvent.ACTION_POINTER_UP) {
			this.optimizeActiveLines();
		}
		return true;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		// White background
		canvas.drawColor(colorScheme.getBackgroundColor());
		drawGrid(canvas);
		
		for (int i = 0; i < mBackgroundLines.size(); ++i){
			mBackgroundLines.get(i).draw(canvas, transformer);
		}
		
		for (int i = 0; i < tokens.list().size(); ++i){
			tokens.list().get(i).draw(canvas, transformer);
		}
		
		if (this.shouldDrawAnnotations) {
			for (int i = 0; i < mAnnotationLines.size(); ++i){
				mAnnotationLines.get(i).draw(canvas, transformer);
			}
		}
	}

	private void drawGrid(Canvas canvas) {
		paint.setColor(colorScheme.getLineColor());
		
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// At zoom level 1.0, we should have 15 squares across and the correct number of squares down.
		float numSquaresHorizontal = (15 / transformer.zoomLevel);
		float numSquaresVertical = (numSquaresHorizontal * ((float)height)/((float)width));
		
		float squareSize = (float) width / numSquaresHorizontal;
		
		int offsetX = (int)transformer.originX % (int)squareSize;
		int offsetY = (int)transformer.originY % (int)squareSize;
		
		int thickLineStartX = ((int)transformer.originX % (int)(squareSize * 5)) / (int)squareSize;
		int thickLineStartY = ((int)transformer.originY % (int)(squareSize * 5)) / (int)squareSize;
		
		for (int i = 0; i <= numSquaresHorizontal; ++i) {
			if ((i-thickLineStartX)%5 == 0) {
				paint.setStrokeWidth(2);
			}
			else {
				paint.setStrokeWidth(1);
			}
			canvas.drawLine(i * squareSize + offsetX, 0, i * squareSize + offsetX, height, paint);
		}
		
		for (int i = 0; i <= numSquaresVertical; ++i) {
			if ((i-thickLineStartY)%5 == 0) {
				paint.setStrokeWidth(2);
			}
			else {
				paint.setStrokeWidth(1);
			}
			canvas.drawLine(0, i * squareSize + offsetY, width, i * squareSize + offsetY, paint);
		}
	}
	
	public CoordinateTransformer getTransformer() {
		// TODO Auto-generated method stub
		return this.transformer;
	}
	
	public Line createLine() {
		Line l = new Line(this.newLineColor);
		mActiveLines.add(l);
		return l;
	}
	
	public List<Line> getLines() {
		return mActiveLines;
	}

	public void placeTokenRandomly(Token t) {
		Random r = new Random();
		PointF randomPoint = new PointF(r.nextFloat() * this.getWidth(), r.nextFloat() * this.getHeight());
		t.location = this.transformer.screenSpaceToWorldSpace(randomPoint);
		this.tokens.addToken(t);
		invalidate();
	}

	public void clearAll() {
		this.mBackgroundLines.clear();
		this.mAnnotationLines.clear();
		this.tokens.clear();
		invalidate();
	}
	
	public void optimizeActiveLines() {
		List<Line> newLines = new ArrayList<Line>();
		for (int i = 0; i < mActiveLines.size(); ++i) {
			List<Line> optimizedLines = mActiveLines.get(i).removeErasedPoints();
			newLines.addAll(optimizedLines);
		}
		mActiveLines.clear();
		mActiveLines.addAll(newLines);
	}

}
