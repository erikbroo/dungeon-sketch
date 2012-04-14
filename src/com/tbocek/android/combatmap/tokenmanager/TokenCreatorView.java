package com.tbocek.android.combatmap.tokenmanager;

import com.tbocek.android.combatmap.model.primitives.PointF;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

/**
 * This view is the main rendering and manipulation logic for the token creator
 * activity.
 * @author Tim Bocek
 *
 */
public final class TokenCreatorView extends View {
	/**
	 * The maximum dimension to output for a square token.  If a token is too
	 * big it will be scaled down to this when saving.
	 */
    private static final int MAX_TOKEN_SIZE = 150;

    /**
     * Whether a circle has been drawn on the loaded token yet.
     */
    private boolean mHasCircle;

    /**
     * X coordinate of the center of the circle drawn on the candidate image,
     * in screen space.
     */
    private float mCircleCenterX;

    /**
     * Y coordinate of the center of the circle drawn on the candidate image,
     * in screen space.
     */
    private float mCircleCenterY;

    /**
     * Radius of the circle drawn on the candidate image, in screen space.
     */
    private float mCircleRadius;

    /**
     * The current image that is being cropped.
     */
    private Drawable mCurrentImage;

    /**
     * Detector that will allow the user to move the candidate circle.
     */
    private GestureDetector mGestureDetector;

    /**
     * Detector that will allow the user to pinch zoom to resize the candidate
     * circle.
     */
    private ScaleGestureDetector mScaleDetector;

    /**
     * Gesture listener that moves the candidate circle when the user scrolls.
     */
    private SimpleOnGestureListener mOnGesture = new SimpleOnGestureListener() {
        @Override
        public boolean onScroll(
        		final MotionEvent e1, final MotionEvent e2,
        		final float distanceX, final float distanceY) {
            mCircleCenterX -= distanceX;
            mCircleCenterY -= distanceY;
            invalidate();
            return true;
        }
    };

    /**
     * Gesture listener that resizes the candidate circle when the user pinch
     * zooms.
     */
    private SimpleOnScaleGestureListener mOnScaleGesture =
    		new SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(final ScaleGestureDetector detector) {
            mCircleCenterX = detector.getFocusX();
            mCircleCenterY = detector.getFocusY();
            mCircleRadius = detector.getCurrentSpan() / 2;
            mHasCircle = true;
            invalidate();
            return true;
        }
    };

    /**
     * Constructor.
     * @param context The context this view is being constructed in.
     */
    public TokenCreatorView(final Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mGestureDetector = new GestureDetector(getContext(), mOnGesture);
        mScaleDetector = new ScaleGestureDetector(
        		getContext(), mOnScaleGesture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * Sets the candidate image that the user will crop.
     * @param image The image to eventually use as a token.
     */
    public void setImage(final Drawable image) {
        mCurrentImage = image;
        // New image, so clear the circle.
        mHasCircle = false;
	    if (this.getWidth() > 0) {
	    	setCircleToDefault(this.getWidth(), this.getHeight());
	    } else {
	    	mHasCircle = false; // Defer until the widget is sized into the layout.
	    }
	        
        invalidate();
    }

	@Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	if (!mHasCircle && w > 0 && h > 0) {
    		setCircleToDefault(w,h);
    	}
    }
    
    /**
     * Sets the currently cut out circle to a default.
     * @param width The width of the view
     * @param height The height of the view
     */
    private void setCircleToDefault(float width, float height) {
    	if (width > 0 && height > 0 && this.mCurrentImage != null && this.mCurrentImage.getIntrinsicWidth() > 0) {
    		this.mCircleCenterX = width / 2;
    		this.mCircleCenterY = height / 2;
    		
    		float imgWidth = this.mCurrentImage.getIntrinsicWidth();
    		float imgHeight = this.mCurrentImage.getIntrinsicHeight();
    		
    		// Check the aspect ratio to see if width or height will be
    		// the limiting factor
    		if (width / height > imgWidth / imgHeight) {
    			// Image is thinner than the current view, height will be the
    			// limiting factor.
    			if (imgWidth / imgHeight < 1) {
    				this.mCircleRadius = .5f * imgWidth * (height / imgHeight);
    			} else {
    				this.mCircleRadius = height / 2;
    			}
    		} else {
    			if (imgWidth / imgHeight < 1) {
    				this.mCircleRadius = width / 2;
    			} else {
    				this.mCircleRadius = .5f * imgHeight * (width / imgWidth);
    			}
    		}
    		this.mHasCircle = true;
    	}
    }
    
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
    	// Forward touch events to the gesture detectors.
        this.mGestureDetector.onTouchEvent(ev);
        this.mScaleDetector.onTouchEvent(ev);
        return true;
    }

    @Override
    public void onDraw(final Canvas canvas) {
        if (mCurrentImage == null) {
        	return;
        }

        mCurrentImage.setBounds(getImageRect());
        if (mHasCircle) {
            // If a circle is being drawn, draw a half-brightness version of
            // the full image followed by a full-brightness version of the
            // clipped region
            drawHalfBrightnessImage(canvas);
            drawFullBrightnessCircle(canvas);
        } else {
        	mCurrentImage.draw(canvas);
        }
    }

    /**
     * Draws the image at half brightness.
     * @param canvas The canvas to draw on.
     */
    public void drawHalfBrightnessImage(final Canvas canvas) {
        //TODO: Cache the result of the color filter
        mCurrentImage.setColorFilter(
        		new PorterDuffColorFilter(
        				Color.GRAY, PorterDuff.Mode.MULTIPLY));
        mCurrentImage.draw(canvas);
        mCurrentImage.setColorFilter(null);
    }

    /**
     * Draws the full brightness image only in the selected circle.
     * @param canvas The canvas to draw on.
     */
    public void drawFullBrightnessCircle(final Canvas canvas) {
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        Path p = new Path();
        p.addCircle(
        		mCircleCenterX, mCircleCenterY, mCircleRadius, Path.Direction.CW);
        canvas.clipPath(p);
        mCurrentImage.draw(canvas);
        canvas.restore();
    }

    /**
     * Gets a bitmap that has the selected circle inscribed in it.  The circle
     * is not actually drawn in the bitmap, but is exported such that other
     * circle drawing methods will draw it correctly.  The bitmap may also be
     * downscaled if it is large.
     * @return The bitmap suitable for writing to the token database.
     */
    public Bitmap getClippedBitmap() {
        //TODO: Downscale in the case of a large image.
        if (mCurrentImage == null || !mHasCircle) {
        	return null;
        }
        float scale = getScaleFactor();

        float squareSizeImageSpace = 2 * mCircleRadius * scale;
        int bitmapSquareSize =
        	Math.min((int) squareSizeImageSpace, MAX_TOKEN_SIZE);

        Bitmap bitmap = Bitmap.createBitmap(
                bitmapSquareSize, bitmapSquareSize, Bitmap.Config.ARGB_8888);

        // Compute a clipping rectangle that is intentionally larger than the
        // bitmap and allows the bitmap to sit such that the drawn circle is
        // inscribed in it.
        PointF centerImageSpace = getCenterInImageSpace();
        PointF upperLeftImageSpace = new PointF(
                centerImageSpace.x - squareSizeImageSpace / 2,
                centerImageSpace.y - squareSizeImageSpace / 2);

        Rect clippingRect = new Rect(
                (int) (-upperLeftImageSpace.x),
                (int) (-upperLeftImageSpace.y),
                (int) (-upperLeftImageSpace.x
                		+ mCurrentImage.getIntrinsicWidth()),
                (int) (-upperLeftImageSpace.y
                		+ mCurrentImage.getIntrinsicHeight()));

        Canvas canvas = new Canvas(bitmap);
        float outputScale = ((float) bitmapSquareSize) / squareSizeImageSpace;
        canvas.scale(outputScale, outputScale);
        mCurrentImage.setBounds(clippingRect);
        mCurrentImage.draw(canvas);

        return bitmap;
    }


    /**
     * Returns a rectangle with the same aspect ratio as the image that
     * represents the area of this view that the image should occupy.
     * @return The rectangle.
     */
    private Rect getImageRect() {
        if (mCurrentImage == null) {
        	return null;
        }

        float screenAspectRatio = ((float) getWidth()) / ((float) getHeight());
        float imageAspectRatio = ((float) mCurrentImage.getIntrinsicWidth())
        		/ ((float) mCurrentImage.getIntrinsicHeight());

        float width;
        float height;
        if (imageAspectRatio > screenAspectRatio) {
            // Image is wider than the screen, fit width to screen and center
        	// vertically
            width = getWidth();
            height = width / imageAspectRatio;
        } else {
            // Image is taller than the screen, fit height to screen and center
        	// horizontally
            height = getHeight();
            width = height * imageAspectRatio;
        }
        float startX = (getWidth() - width) / 2;
        float startY = (getHeight() - height) / 2;
        return new Rect(
        		(int) startX, (int) startY,
        		(int) (startX + width), (int) (startY + height));
    }

    /**
     * Returns the ratio of length in image space to length in screen space.
     * @return The scale factor.
     */
    private float getScaleFactor() {
        if (mCurrentImage == null) {
        	return 1f;
        }

        float screenAspectRatio = ((float) getWidth()) / ((float) getHeight());
        float imageAspectRatio = ((float) mCurrentImage.getIntrinsicWidth())
        		/ ((float) mCurrentImage.getIntrinsicHeight());

        if (imageAspectRatio > screenAspectRatio) {
            // Screen width is limiting factor
            return ((float) mCurrentImage.getIntrinsicWidth())
            		/ ((float) getWidth());
        } else {
            // Screen height is limiting factor
            return ((float) mCurrentImage.getIntrinsicHeight())
            		/ ((float) getHeight());
        }

    }

    /**
     * Transforms the center of the circle in screen space to the center of
     * the circle in image space.
     * @return The center in image space.
     */
    private PointF getCenterInImageSpace() {
        // First, compute where the center would be if there were no scale
    	// factor but the origin  was at the upper left hand corner of the clip
    	// rectangle
        Rect clipRect = getImageRect();
        float imageClipSpaceX = mCircleCenterX - clipRect.left;
        float imageClipSpaceY = mCircleCenterY - clipRect.top;

        // Now, scale these coordinates using the scale factor
        float scaleFactor = getScaleFactor();
        return new PointF(
        		imageClipSpaceX * scaleFactor, imageClipSpaceY * scaleFactor);
    }
}
