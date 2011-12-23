package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.Util;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
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
            onChangeDrawToolListener.onChooseColoredPen(mColor);
            untoggleGroup(colorGroup);
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
            onChangeDrawToolListener.onChooseStrokeWidth(mWidth);
            untoggleGroup(lineWidthGroup);
            ((ImageToggleButton) v).setToggled(true);
            setGroupVisibility(colorGroup, View.VISIBLE);
        }
    }

    /**
     * The button used to select the mask control.  Needs to be stored because
     * it needs to be conditionally hidden.
     */
    private ImageToggleButton mMaskButton;

    private HorizontalScrollView innerView;

    /**
     * Constructs a new DrawOptionsView.
     * @param context The context to construct in.
     */
    public DrawOptionsView(final Context context) {
        super(context);
        createAndAddPanButton();

        layout = new LinearLayout(context);
        innerView = new HorizontalScrollView(context);
        innerView.addView(layout);
        addView(innerView);

        createAndAddEraserButton();
        createAndAddStraightLineButton();
        createAndAddFreehandLineButton();
        createAndAddCircleButton();


        createAndAddSeperator();

        createAndAddUndoButton();
        createAndAddRedoButton();

        createAndAddSeperator();

        addStrokeWidthButton(.05f, R.drawable.pencil);
        addStrokeWidthButton(.1f, R.drawable.pen);
        addStrokeWidthButton(.5f, R.drawable.paintbrush);
        addStrokeWidthButton(2.0f, R.drawable.inktube);
        createAndAddFillButton();
        mMaskButton = createAndAddMaskButton();

        createAndAddSeperator();

        for (int color : Util.getStandardColorPalette()) {
            addColorButton(color);
        }
    }


	/**
	 *
	 */
	protected void createAndAddFillButton() {
        ImageToggleButton b = new ImageToggleButton(this.getContext());
        b.setImageResource(R.drawable.freehand_shape);
        b.setOnClickListener(new StrokeWidthListener(Float.POSITIVE_INFINITY));
        layout.addView(b);
        lineWidthGroup.add(b);
        lineWidthRegionGroup.add(b);
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
        layout.addView(seperator);
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
				onChangeDrawToolListener.onChooseMaskTool();
                untoggleGroup(lineWidthGroup);
                maskButton.setToggled(true);
                setGroupVisibility(colorGroup, View.GONE);
			}
        });
        layout.addView(maskButton);
        return maskButton;
	}


    /**
     * Sets whether the mask tool should be visible.
     * @param visible True if visible.
     */
    public void setMaskToolVisibility(final boolean visible) {
    	mMaskButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    	if (visible) {
            lineWidthGroup.add(mMaskButton);
            lineWidthRegionGroup.add(mMaskButton);
    	} else {
            lineWidthGroup.remove(mMaskButton);
            lineWidthRegionGroup.remove(mMaskButton);
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
                (int) (COLOR_BUTTON_SIZE * getResources().getDisplayMetrics().density),
                (int) (COLOR_BUTTON_SIZE * getResources().getDisplayMetrics().density)));
        Drawable pencil =
            this.getContext().getResources().getDrawable(R.drawable.pencilbw);
        b.setImageDrawable(pencil);
        b.setColorFilter(
                new PorterDuffColorFilter(color, PorterDuff.Mode.LIGHTEN));
        layout.addView(b);
        colorGroup.add(b);
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
        layout.addView(b);
        lineWidthGroup.add(b);
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
         * Fired when the mask took is selected.
         */
        void onChooseMaskTool();

        /**
         * Fired when the delete region tool is selected.
         */
        void onChooseDeleteTool();

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
         * Fired when the undo button is clicked.
         */
        void onClickUndo();

        /**
         * Fired when the redo button is clicked.
         */
        void onClickRedo();

		void onChooseFreeHandTool();

		void onChooseStraightLineTool();

		void onChooseCircleTool();
    }

    /**
     * Using the null class pattern; this implementation of
     * OnChangeDrawToolListener should do nothing.  It is only here to avoid
     * null checkes whenever one of these methods is called.
     * @author Tim
     *
     */
    public class NullChangeDrawToolListener
    		implements OnChangeDrawToolListener {
        @Override
        public void onChooseEraser() { }

        @Override
        public void onChooseDeleteTool() { }

        @Override
        public void onChooseColoredPen(final int color) { }

        @Override
        public void onChoosePanTool() { }

        @Override
        public void onChooseStrokeWidth(final float width) { }

		@Override
		public void onClickUndo() { }

		@Override
		public void onClickRedo() { }

		@Override
		public void onChooseMaskTool() { }

		@Override
		public void onChooseFreeHandTool() { }

		@Override
		public void onChooseStraightLineTool() { }

		@Override
		public void onChooseCircleTool() { }
    }


    /**
     * The listener that is called when something about the current draw tool
     * changes.
     */
    protected OnChangeDrawToolListener onChangeDrawToolListener =
        new NullChangeDrawToolListener();

    /**
     * Sets the listener to call when a new draw tool is selected.
     * @param listener The new listener
     */
    public void setOnChangeDrawToolListener(
            final OnChangeDrawToolListener listener) {
        this.onChangeDrawToolListener = listener;
    }

    /**
     * The layout that will hold drawing buttons.
     */
    protected LinearLayout layout;

    /**
     * A list of all buttons that select a drawing tool, so that they can be
     * modified as a group.
     */
    protected List<ImageToggleButton> toolsGroup =
        new ArrayList<ImageToggleButton>();

    /**
     * A list of all buttons that select a color, so that they can be modified
     * as a group.
     */
    protected List<ImageToggleButton> colorGroup =
        new ArrayList<ImageToggleButton>();

    protected List<ImageToggleButton> lineWidthGroup =
    	new ArrayList<ImageToggleButton>();

    /**
     * Line widths that do not make sense when using tools that don't support
     * drawing a region (like straight lines).
     */
    protected List<ImageToggleButton> lineWidthRegionGroup =
    	new ArrayList<ImageToggleButton>();

    /**
     * Untoggles an entire list of ImageToggleButtons, so we can make sure that
     * only one button in the list is toggled at once.
     * @param group The list of ImageToggleButtons to modify.
     */
    protected void untoggleGroup(final List<ImageToggleButton> group) {
        for (ImageToggleButton b : group) {
            b.setToggled(false);
        }
    }

    /**
     * Automatically loads the default tool.
     */
    public void setDefault() {
        // Start out with the pan button selected.
    	if (defaultView != null) {
    		defaultView.performClick();
    	}
    }
    protected View defaultView;

	/**
	 * Creates the undo button and adds it to the view.
	 */
	protected void createAndAddUndoButton() {
		ImageButton undoButton = new ImageButton(this.getContext());
        undoButton.setImageResource(R.drawable.undo);
        undoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				onChangeDrawToolListener.onClickUndo();
			}
        });
        layout.addView(undoButton);
	}

	/**
	 * Creates the redo button and adds it to the view.
	 */
    protected void createAndAddRedoButton() {
		ImageButton redoButton = new ImageButton(this.getContext());
        redoButton.setImageResource(R.drawable.redo);
        redoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				onChangeDrawToolListener.onClickRedo();
			}
        });
        layout.addView(redoButton);
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
                onChangeDrawToolListener.onChooseEraser();
                untoggleGroup(toolsGroup);
                setGroupVisibility(colorGroup, View.GONE);
                setGroupVisibility(lineWidthGroup, View.GONE);
                eraserButton.setToggled(true);
            }
        });
        layout.addView(eraserButton);
        toolsGroup.add(eraserButton);
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
                onChangeDrawToolListener.onChoosePanTool();
                untoggleGroup(toolsGroup);
                setGroupVisibility(colorGroup, View.GONE);
                setGroupVisibility(lineWidthGroup, View.GONE);
                panButton.setToggled(true);
            }
        });
        this.addView(panButton);
        toolsGroup.add(panButton);
        defaultView = panButton;
	}

	protected void createAndAddStraightLineButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.line_straight);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onChangeDrawToolListener.onChooseStraightLineTool();
                untoggleGroup(toolsGroup);
                setGroupVisibility(colorGroup, View.VISIBLE);
                setGroupVisibility(lineWidthGroup, View.VISIBLE);
                setGroupVisibility(lineWidthRegionGroup, View.GONE);
                button.setToggled(true);
            }
        });
        layout.addView(button);
        toolsGroup.add(button);
	}

	protected void createAndAddFreehandLineButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.line_freehand);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onChangeDrawToolListener.onChooseFreeHandTool();
                untoggleGroup(toolsGroup);
                setGroupVisibility(colorGroup, View.VISIBLE);
                setGroupVisibility(lineWidthGroup, View.VISIBLE);
                button.setToggled(true);

                // HACK: If we were in mask mode, make sure we are still in mask
                // mode.
                if (mMaskButton.isToggled()) {
                	mMaskButton.performClick();
                }
            }
        });
        layout.addView(button);
        toolsGroup.add(button);
	}


	protected void createAndAddCircleButton() {
		final ImageToggleButton button =
			new ImageToggleButton(this.getContext());
        button.setImageResource(R.drawable.circle);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onChangeDrawToolListener.onChooseCircleTool();
                untoggleGroup(toolsGroup);
                setGroupVisibility(colorGroup, View.VISIBLE);
                setGroupVisibility(lineWidthGroup, View.VISIBLE);
                button.setToggled(true);

                // HACK: If we were in mask mode, make sure we are still in mask
                // mode.
                if (mMaskButton.isToggled()) {
                	mMaskButton.performClick();
                }
            }
        });
        layout.addView(button);
        toolsGroup.add(button);
	}


    /**
     * Sets the visibility of a group of buttons.
     * @param group The list of views to modify.
     * @param visibility Visibility to pass to each view's setVisibility method.
     */
    protected void setGroupVisibility(final List<ImageToggleButton> group,
            final int visibility) {
        for (ImageToggleButton b : group) {
            b.setVisibility(visibility);
        }
    }
}
