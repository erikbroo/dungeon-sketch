package com.tbocek.android.combatmap.tokenmanager;

import com.tbocek.android.combatmap.graphicscore.PointF;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

public class TokenCreatorView extends View {
	private static final int MAX_TOKEN_SIZE=300;
	
	private boolean hasCircle = false;
	
	// All of the following in screen space
	private float circleCenterX = 0;
	private float circleCenterY = 0;
	private float circleRadius = 0;
	
	private Drawable currentImage = null;
	
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleDetector;
	
	private SimpleOnGestureListener onGesture = new SimpleOnGestureListener() {
	    @Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	    	circleCenterX -= distanceX;
	    	circleCenterY -= distanceY;
	    	invalidate();
			return true;
		}
	};
	
	private SimpleOnScaleGestureListener onScaleGesture = new SimpleOnScaleGestureListener() {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			circleCenterX = detector.getFocusX();
			circleCenterY = detector.getFocusY();
			circleRadius = detector.getCurrentSpan() / 2;
			hasCircle = true;
			invalidate();
		    return true;
		}
	};
	
	public TokenCreatorView(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);
		gestureDetector = new GestureDetector(getContext(), onGesture);
		scaleDetector = new ScaleGestureDetector(getContext(), onScaleGesture);
	}
	
	public void setImage(Drawable image) {
		currentImage = image;
		hasCircle = false;
		invalidate();
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		this.gestureDetector.onTouchEvent(ev);
		this.scaleDetector.onTouchEvent(ev);
		return true;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if (currentImage == null) return;
		
		currentImage.setBounds(getImageRect());
		if (hasCircle) {
			// If a circle is being drawn, draw a half-brightness version of
			// the full image followed by a full-brightness version of the 
			// clipped region
			drawHalfBrightnessImage(canvas);
			drawFullBrightnessCircle(canvas);
		} else {
			// If a circle is being drawn, draw a half-brightness version of
			// the full image followed by a full-brightness version of the 
			// clipped region
			drawImage(canvas);
		}
	}

	public void drawImage(Canvas canvas) {
		currentImage.draw(canvas);
	}

	public void drawHalfBrightnessImage(Canvas canvas) {
		//TODO: Cache the result of the color filter
		currentImage.setColorFilter( new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY));
		drawImage(canvas);
		currentImage.setColorFilter(null);
	}

	public void drawFullBrightnessCircle(Canvas canvas) {
		//TODO: Set the rect so that we only draw a small region of the screen (not sure if this will actually get a benefit)
		canvas.save(Canvas.CLIP_SAVE_FLAG);
		Path p = new Path();
		p.addCircle(circleCenterX, circleCenterY, circleRadius, Path.Direction.CW);
		canvas.clipPath(p);
		drawImage(canvas);
		canvas.restore();
	}
	
	public Bitmap getClippedBitmap() {
		//TODO: Downscale in the case of a large image.
		if (currentImage == null || !hasCircle) return null;
		float scale = getScaleFactor();
		
		float squareSizeImageSpace = 2 * circleRadius * scale;
		int bitmapSquareSize = Math.min((int)squareSizeImageSpace, MAX_TOKEN_SIZE);
		
		Bitmap bitmap = Bitmap.createBitmap(
				bitmapSquareSize, bitmapSquareSize, Bitmap.Config.ARGB_8888);
		int DEBUG_width = bitmap.getWidth();
		int DEBUG_height = bitmap.getHeight();
		
		// Compute a clipping rectangle that is intentionally larger than the bitmap
		// and allows the bitmap to sit such that the drawn circle is inscribed in it.
		PointF centerImageSpace = getCenterInImageSpace();
		PointF upperLeftImageSpace = new PointF(
				centerImageSpace.x - squareSizeImageSpace / 2,
				centerImageSpace.y - squareSizeImageSpace / 2);
		
		Rect clippingRect = new Rect(
				(int)(-upperLeftImageSpace.x), 
				(int)(-upperLeftImageSpace.y), 
				(int)(-upperLeftImageSpace.x + currentImage.getIntrinsicWidth()), 
				(int)(-upperLeftImageSpace.y + currentImage.getIntrinsicHeight()));
		
		Canvas canvas = new Canvas(bitmap);
		float outputScale = ((float)bitmapSquareSize)/squareSizeImageSpace;
		canvas.scale(outputScale, outputScale);
		currentImage.setBounds(clippingRect);
		currentImage.draw(canvas);
		
		return bitmap;
	}
	

	/**
	 * Returns a rectangle with the same aspect ratio as the image that represents
	 * the area of this view that the image should occupy.
	 * @return
	 */
	private Rect getImageRect() {
		if (currentImage == null) return null;
		
		float screenAspectRatio = ((float)getWidth())/((float)getHeight());
		float imageAspectRatio = ((float)currentImage.getIntrinsicWidth())/((float)currentImage.getIntrinsicHeight());
		
		float width;
		float height;
		if (imageAspectRatio > screenAspectRatio) {
			// Image is wider than the screen, fit width to screen and center vertically
			width = getWidth();
			height = width / imageAspectRatio;
		} else {
			// Image is taller than the screen, fit height to screen and center horizontally
			height = getHeight();
			width = height * imageAspectRatio;		
		}
		float startX = (getWidth() - width) / 2;
		float startY = (getHeight() - height) / 2;
		return new Rect((int)(startX), (int)(startY), (int)(startX + width), (int)(startY + height));
	}
	
	/**
	 * Returns the ratio of length in image space to length in screen space
	 * @return
	 */
	private float getScaleFactor() {
		if (currentImage == null) return 1f;
		
		float screenAspectRatio = ((float)getWidth())/((float)getHeight());
		float imageAspectRatio = ((float)currentImage.getIntrinsicWidth())/((float)currentImage.getIntrinsicHeight());
		
		if (imageAspectRatio > screenAspectRatio) {
			// Screen width is limiting factor
			return ((float)currentImage.getIntrinsicWidth()) / ((float)getWidth());
		} else {
			// Screen height is limiting factor
			return ((float)currentImage.getIntrinsicHeight()) / ((float)getHeight());
		}
		
	}
	
	private PointF getCenterInImageSpace() {
		// First, compute where the center would be if there were no scale factor but the origin
		// was at the upper left hand corner of the clip rectangle
		Rect clipRect = getImageRect();
		float imageClipSpaceX = circleCenterX - clipRect.left;
		float imageClipSpaceY = circleCenterY - clipRect.top;
		
		// Now, scale these coordinates using the scale factor
		float scaleFactor = getScaleFactor();
		return new PointF(imageClipSpaceX * scaleFactor, imageClipSpaceY * scaleFactor);
	}
}
