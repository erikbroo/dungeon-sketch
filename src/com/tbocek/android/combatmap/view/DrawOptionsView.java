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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Provides a tool and color selector for drawing.
 * @author Tim Bocek
 *
 */
public final class DrawOptionsView extends DrawOptionsViewBase {

    /**
     * The width on each side of the buttons used to pick a new color.
     */
    private static final int COLOR_BUTTON_SIZE = 48;

    /**
     * The width of the space that seperates tools and colors.
     */
    private static final int SEPERATOR_WIDTH = 20;

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
            untoggleGroup(toolsGroup);
            setGroupVisibility(colorGroup, View.VISIBLE);
            ((ImageToggleButton) v).setToggled(true);
        }
    }

    /**
     * The button used to select the mask control.  Needs to be stored because
     * it needs to be conditionally hidden.
     */
    private View mMaskButton;

    /**
     * Constructs a new DrawOptionsView.
     * @param context The context to construct in.
     */
    public DrawOptionsView(final Context context) {
        super(context);
        layout = new LinearLayout(context);
        addView(layout);

        createAndAddPanButton();
        createAndAddUndoButton();
        createAndAddRedoButton();
        createAndAddEraserButton();

        addStrokeWidthButton(.05f, R.drawable.pencil);
        addStrokeWidthButton(.1f, R.drawable.pen);
        addStrokeWidthButton(.5f, R.drawable.paintbrush);
        addStrokeWidthButton(2.0f, R.drawable.inktube);
        addStrokeWidthButton(Float.POSITIVE_INFINITY, R.drawable.freehand_shape);

        mMaskButton = createAndAddMaskButton();

        createAndAddLineModeToggle();

        //Create a seperator
        ImageView seperator = new ImageView(this.getContext());
        seperator.setLayoutParams(
                new LinearLayout.LayoutParams(
                        SEPERATOR_WIDTH,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        layout.addView(seperator);

        for (int color : Util.getStandardColorPalette()) {
            addColorButton(color);
        }
    }


    /**
     * Creates and adds a button that toggles straight line drawing on and off.
     */
    private void createAndAddLineModeToggle() {
    	TwoImageToggleButton toggle =
    		new TwoImageToggleButton(
    				this.getContext(), R.drawable.line_freehand,
    				R.drawable.line_straight);
    	toggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				TwoImageToggleButton button = (TwoImageToggleButton) v;
				button.setToggled(!button.isToggled());
				onChangeDrawToolListener.onSetStraightLineMode(
						button.isToggled());

			}
    	});
    	layout.addView(toggle);
	}


	/**
     * Creates a button to activate the mask tool and adds it to the layout.
     * @return The button to activate the mask tool.
     */
    private View createAndAddMaskButton() {
		final ImageToggleButton maskButton =
				new ImageToggleButton(this.getContext());
        maskButton.setImageResource(R.drawable.mask);
        maskButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				onChangeDrawToolListener.onChooseMaskTool();
                untoggleGroup(toolsGroup);
                setGroupVisibility(colorGroup, View.GONE);
                maskButton.setToggled(true);
			}
        });
        layout.addView(maskButton);
        toolsGroup.add(maskButton);
        return maskButton;
	}


    /**
     * Sets whether the mask tool should be visible.
     * @param visible True if visible.
     */
    public void setMaskToolVisibility(final boolean visible) {
    	mMaskButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


	/**
     * Adds a button that selects the given color as the current color.
     * @param color The color that this button will select.
     */
    private void addColorButton(final int color) {
        ImageToggleButton b = new ImageToggleButton(this.getContext());
        b.setOnClickListener(new ColorListener(color));
        b.setLayoutParams(new LinearLayout.LayoutParams(
                COLOR_BUTTON_SIZE, COLOR_BUTTON_SIZE));
        Drawable pencil =
            this.getContext().getResources().getDrawable(R.drawable.pencilbw);
        b.setImageDrawable(pencil);
        b.setColorFilter(
                new PorterDuffColorFilter(color, PorterDuff.Mode.OVERLAY));
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
        toolsGroup.add(b);
    }
}
