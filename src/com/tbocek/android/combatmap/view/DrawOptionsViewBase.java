package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class DrawOptionsViewBase extends HorizontalScrollView {

	public DrawOptionsViewBase(Context context) {
		super(context);
	}

	public DrawOptionsViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawOptionsViewBase(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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
                panButton.setToggled(true);
            }
        });
        layout.addView(panButton);
        toolsGroup.add(panButton);
        defaultView = panButton;
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