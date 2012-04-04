package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.model.primitives.Util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Provides a tool and color selector for drawing.
 * @author Tim Bocek
 *
 */
public final class DrawOptionsView extends LinearLayout {

    /**
     * The width on each side of the buttons used to pick a new color.
     */
    private static final int COLOR_BUTTON_SIZE = 48;

    /**
     * The width of the space that seperates tools and colors.
     */
    private static final int SEPERATOR_WIDTH = 32;
    
    /**
     * Stroke width to use for the pencil tool.
     */
    private static final float PENCIL_STROKE_WIDTH = 0.05f;
    
    /**
     * Stroke width to use for the pen tool.
     */
    private static final float PEN_STROKE_WIDTH = 0.1f;
    
    /**
     * Stroke width to use for the paintbrush tool.
     */
    private static final float PAINTBRUSH_STROKE_WIDTH = 0.5f;
    
    /**
     * Stroke width to use for the ink tube tool.
     */
    private static final float INKTUBE_STROKE_WIDTH = 2.0f;

    /**
     * The listener that is called when something about the current draw tool
     * changes.
     */
    private OnChangeDrawToolListener mOnChangeDrawToolListener =
        new NullChangeDrawToolListener();
    
    /**
     * The layout that will hold drawing buttons.
     */
    private LinearLayout mLayout;

    /**
     * A list of all buttons that select a drawing tool, so that they can be
     * modified as a group.
     */
    private ToggleButtonGroup mToolsGroup =  new ToggleButtonGroup();

    /**
     * A list of all buttons that select a color, so that they can be modified
     * as a group.
     */
    private ToggleButtonGroup mColorGroup = new ToggleButtonGroup();

    /**
     * List of all buttons that select line widths, so that they can be modified
     * as a group.
     */
    private ToggleButtonGroup mLineWidthGroup = new ToggleButtonGroup();

    /**
     * Line widths that do not make sense when using tools that don't support
     * drawing a region (like straight lines).
     */
    private ToggleButtonGroup mLineWidthRegionGroup = new ToggleButtonGroup();
    
    /**
     * Button that loads images onto the background.  Should only show in
     * background mode.
     */
    private ImageToggleButton mBackgroundImageButton;
    
    /**
     * OnClickListener for when a button representing a color is clicked.
     * @author Tim Bocek
     */
    private class ColorListener implements View.OnClickListener {
        /**
         * The color that this listener will pick when fired.
         */
        private int mColor;

        /**
         * Constructor.
         * @param color The color that the listener will pick when fired.
         */
        public ColorListener(final int color) {
            this.mColor = color;
        }

        @Override
        public void onClick(final View v) {
            mOnChangeDrawToolListener.onChooseColoredPen(mColor);
            mColorGroup.untoggle();
            ((ImageToggleButton) v).setToggled(true);
        }
    }

    /**
     * OnClickListener for when a button representing a drawing width is
     * clicked.
     * @author Tim Bocek
     */
    private class StrokeWidthListener implements View.OnClickListener {
        /**
         * The line width that the drawing tool will be changed to when this
         * listener fires.
         */
        private float mWidth;

        /**
         * Constructor.
         * @param f The line width that will be used when the listener
         * 		fires.
         */
        public StrokeWidthListener(final float f) {
            this.mWidth = f;
        }

        @Override
        public void onClick(final View v) {
            mOnChangeDrawToolListener.onChooseStrokeWidth(mWidth);
            mLineWidthGroup.untoggle();
            ((ImageToggleButton) v).setToggled(true);
            mColorGroup.setGroupVisibility(View.VISIBLE);
        }
    }

    /**
     * The button used to select the mask control.  Needs to be stored because
     * it needs to be conditionally hidden.
     */
    private ImageToggleButton mMaskButton;

    /**
     * View that contains controls that should be scrolled (this is not all
     * controls because we want the pan control to "stick" and be always
     * visible).
     */
    private HorizontalScrollView mInnerView;

    /**
     * Constructs a new DrawOptionsView.
     * @param context The context to construct in.
     */
    public DrawOptionsView(final Context context) {
        super(context);
        createAndAddPanButton();

        mLayout = new LinearLayout(context);
        mInnerView = new HorizontalScrollView(context);
        mInnerView.addView(mLayout);
        addView(mInnerView);

        createAndAddEraserButton();
        createAndAddStraightLineButton();
        createAndAddFreehandLineButton();
        createAndAddRectangleButton();
        createAndAddCircleButton();
        createAndAddTextButton();
        mMaskButton = createAndAddMaskButton();
        
        mBackgroundImageButton = createAndAddBackgroundImageButton();

        createAndAddSeperator();

        addStrokeWidthButton(PENCIL_STROKE_WIDTH, R.drawable.pencil);
        addStrokeWidthButton(PEN_STROKE_WIDTH, R.drawable.pen);
        addStrokeWidthButton(PAINTBRUSH_STROKE_WIDTH, R.drawable.paintbrush);
        addStrokeWidthButton(INKTUBE_STROKE_WIDTH, R.drawable.inktube);
        createAndAddFillButton();

        createAndAddSeperator();

        for (int color : Util.getStandardColorPalette()) {
            addColorButton(color);
        }
    }

    /**
     * Creates a button to enter background image mode.
     * @return The created button.
     */
	private ImageToggleButton createAndAddBackgroundImageButton() {
		final ImageToggleButton b = new ImageToggleButton(this.getContext());
		b.setImageResource(R.drawable.add_image);
		mLayout.addView(b);
		mToolsGroup.add(b);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mLineWidthGroup.setGroupVisibility(View.GONE);
				mColorGroup.setGroupVisibility(View.GONE);
				mToolsGroup.untoggle();
				b.setToggled(true);
				mOnChangeDrawToolListener.onChooseImageTool();
			}
		});
		return b;
	}
	
	/**
	 * Sets whether the image toggle button was visible.
	 * @param visible Whether the button is visible.
	 */
	public void setBackgroundImageButtonVisibility(boolean visible) {
		this.mBackgroundImageButton.setVisibility(
				visible ? View.VISIBLE : View.GONE);
		if (!visible && mBackgroundImageButton.isToggled()) {
			this.mToolsGroup.forceDefault();
		}
	}


	/**
	 *
	 */
	protected void createAndAddFillButton() {
        ImageToggleButton b = new ImageToggleButton(this.getContext());
        b.setImageResource(R.drawable.freehand_shape);
        b.setOnClickListener(new StrokeWidthListener(Float.POSITIVE_INFINITY));
        mLayout.addView(b);
        mLineWidthGroup.add(b);
        mLineWidthRegionGroup.add(b);
	}


	/**
	 *
	 */
	protected void createAndAddSeperator() {
		ImageView seperator = new ImageView(this.getContext());
        seperator.setLayoutParams(
                new LinearLayout.LayoutParams(
                        SEPERATOR_WIDTH,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        mLayout.addView(seperator);
	}


	/**
     * Creates a button to activate the mask tool and adds it to the layout.
     * @return The button to activate the mask tool.
     */
    private ImageToggleButton createAndAddMaskButton() {
		final ImageToggleButton maskButton =
				new ImageToggleButton(this.getContext());
        maskButton.setImageResource(R.drawable.mask);
        maskButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				mOnChangeDrawToolListener.onChooseMaskTool();
                mToolsGroup.untoggle();
                maskButton.setToggled(true);
                mColorGroup.setGroupVisibility(View.GONE);
                mLineWidthGroup.setGroupVisibility(View.GONE);
			}
        });
        mLayout.addView(maskButton);
        return maskButton;
	}


    /**
     * Sets whether the mask tool should be visible.
     * @param visible True if visible.
     */
    public void setMaskToolVisibility(final boolean visible) {
    	mMaskButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    	if (visible) {
            mToolsGroup.add(mMaskButton);
    	} else {
    		// If mask tool was selected, we need to de-select it.
    		if (mMaskButton.isToggled()) {
    			mToolsGroup.forceDefault();
    		}
    		mToolsGroup.remove(mMaskButton);
    	}
    }


	/**
     * Adds a button that selects the given color as the current color.
     * @param color The color that this button will select.
     */
    private void addColorButton(final int color) {
        ImageToggleButton b = new ImageToggleButton(this.getContext());
        b.setOnClickListener(new ColorListener(color));
        b.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (COLOR_BUTTON_SIZE 
                		* getResources().getDisplayMetrics().density),
                (int) (COLOR_BUTTON_SIZE 
                		* getResources().getDisplayMetrics().density)));
        Drawable pencil =
            this.getContext().getResources().getDrawable(R.drawable.pencilbw);
        b.setImageDrawable(pencil);
        b.setColorFilter(
                new PorterDuffColorFilter(color, PorterDuff.Mode.LIGHTEN));
        mLayout.addView(b);
        mColorGroup.add(b);
    }

    /**
     * Adds a button that changes the stroke width to the given width, and that
     * uses the given resource ID to represent its self.
     * @param f The width that this button will change the stroke width to.
     * @param resourceId ID of the image to draw on this button.
     */
    private void addStrokeWidthButton(final float f, final int resourceId) {
        ImageToggleButton b = new ImageToggleButton(this.getContext());
        b.setImageResource(resourceId);
        b.setOnClickListener(new StrokeWidthListener(f));
        mLayout.addView(b);
        mLineWidthGroup.add(b);
    }


    /**
     * Listener that is called when different drawing tools are chosen.
     * @author Tim Bocek
     */
    public interface OnChangeDrawToolListener {
        /**
         * Fired when the eraser tool is selected.
         */
        void onChooseEraser();

        /**
         * Fired when the image tool is selected.
         */
        void onChooseImageTool();

		/**
         * Fired when the mask took is selected.
         */
        void onChooseMaskTool();

        /**
         * Fired when the color is changed.
         * @param color The new color.
         */
        void onChooseColoredPen(int color);

        /**
         * Fired when the pan tool is selected.
         */
        void onChoosePanTool();

        /**
         * Fired when the stroke width is changed.  This can also be thought of
         * as selecting a pen tool with the given stroke width.
         * @param width The new stroke width.
         */
        void onChooseStrokeWidth(float width);

        /**
         * Called when the freehand draw tool is chosen.
         */
		void onChooseFreeHandTool();

		/**
		 * Called when the straight line draw tool is chosen.
		 */
		void onChooseStraightLineTool();

		/**
		 * Called when the circle draw tool is chosen.
		 */
		void onChooseCircleTool();
		
		/**
		 * Called when the rectangle draw tool is chosen.
		 */
		void onChooseRectangleTool();

		/**
		 * Called when the text draw tool is chosen.
		 */
		void onChooseTextTool();
    }

    /**
     * Using the null class pattern; this implementation of
     * OnChangeDrawToolListener should do nothing.  It is only here to avoid
     * null checkes whenever one of these methods is called.
     * @author Tim
     *
     */
    public static class NullChangeDrawToolListener
    		implements OnChangeDrawToolListener {
        @Override
        public void onChooseEraser() { }

        @Override
        public void onChooseColoredPen(final int color) { }

        @Override
        public void onChoosePanTool() { }

        @Override
        public void onChooseStrokeWidth(final float width) { }

		@Override
		public void onChooseMaskTool() { }

		@Override
		public void onChooseFreeHandTool() { }

		@Override
		public void onChooseStraightLineTool() { }

		@Override
		public void onChooseCircleTool() { }

		@Override
		public void onChooseTextTool() { }

		@Override
		public void onChooseRectangleTool() { }

		@Override
		public void onChooseImageTool() { }
    }


    /**
     * Sets the listener to call when a new draw tool is selected.
     * @param listener The new listener
     */
    public void setOnChangeDrawToolListener(
            final OnChangeDrawToolListener listener) {
        this.mOnChangeDrawToolListener = listener;
    }

    /**
     * Automatically loads the default tool.  If a tool is already selected,
     * re-selects it.
     */
    public void setDefault() {
        // Start out with the pan button selected.
    	mToolsGroup.maybeSelectDefault();
    }

	/**
	 * Creates the eraser button and adds it to the view.
	 */
	protected void createAndAddEraserButton() {
		final ImageToggleButton eraserButton =
			new ImageToggleButton(this.getContext());
        eraserButton.setImageResource(R.drawable.eraser);
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChooseEraser();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.GONE);
                mLineWidthGroup.setGroupVisibility(View.GONE);
                eraserButton.setToggled(true);
            }
        });
        mLayout.addView(eraserButton);
        mToolsGroup.add(eraserButton);
	}

	/**
     * Creates the pan button, adds it to the view, and sets it as the default.
	 */
	protected void createAndAddPanButton() {
		final ImageToggleButton panButton =
				new ImageToggleButton(this.getContext());
        panButton.setImageResource(R.drawable.transform_move);
        panButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChoosePanTool();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.GONE);
                mLineWidthGroup.setGroupVisibility(View.GONE);
                panButton.setToggled(true);
            }
        });
        this.addView(panButton);
        mToolsGroup.add(panButton);
	}

	/**
	 * Creates a button to switch to straight line drawing and adds it to the
	 * view.
	 */
	protected void createAndAddStraightLineButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.line_straight);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChooseStraightLineTool();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.VISIBLE);
                mLineWidthGroup.setGroupVisibility(View.VISIBLE);
                mLineWidthRegionGroup.setGroupVisibility(View.GONE);
                mLineWidthGroup.maybeSelectDefault();
                mColorGroup.maybeSelectDefault();
                button.setToggled(true);
            }
        });
        mLayout.addView(button);
        mToolsGroup.add(button);
	}

	/**
	 * Creates a button to switch to freehand drawing and adds it to the view.
	 */
	protected void createAndAddFreehandLineButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.line_freehand);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChooseFreeHandTool();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.VISIBLE);
                mLineWidthGroup.setGroupVisibility(View.VISIBLE);
                button.setToggled(true);
                mLineWidthGroup.maybeSelectDefault();
                mColorGroup.maybeSelectDefault();
            }
        });
        mLayout.addView(button);
        mToolsGroup.add(button);
	}

	/**
	 * Creates a button to switch to draw circle mode and adds it to the view.
	 */
	protected void createAndAddCircleButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.circle);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChooseCircleTool();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.VISIBLE);
                mLineWidthGroup.setGroupVisibility(View.VISIBLE);
                button.setToggled(true);
                mLineWidthGroup.maybeSelectDefault();
                mColorGroup.maybeSelectDefault();
            }
        });
        mLayout.addView(button);
        mToolsGroup.add(button);
	}
	
	/**
	 * Creates a button to switch to draw rectangle mode and adds it to the view.
	 */
	protected void createAndAddRectangleButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.rectangle);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChooseRectangleTool();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.VISIBLE);
                mLineWidthGroup.setGroupVisibility(View.VISIBLE);
                button.setToggled(true);
                mLineWidthGroup.maybeSelectDefault();
                mColorGroup.maybeSelectDefault();
            }
        });
        mLayout.addView(button);
        mToolsGroup.add(button);
	}

	/**
	 * Creates a button to switch to draw text tool and adds it to the view.
	 */
	protected void createAndAddTextButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());

        button.setImageResource(R.drawable.draw_text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mOnChangeDrawToolListener.onChooseTextTool();
                mToolsGroup.untoggle();
                mColorGroup.setGroupVisibility(View.VISIBLE);
                mLineWidthGroup.setGroupVisibility(View.GONE);
                button.setToggled(true);
                mLineWidthGroup.maybeSelectDefault();
                mColorGroup.maybeSelectDefault();
            }
        });
        mLayout.addView(button);
        mToolsGroup.add(button);
	}
}
