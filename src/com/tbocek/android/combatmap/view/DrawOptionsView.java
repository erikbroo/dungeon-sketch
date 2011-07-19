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
import android.widget.ImageView;
import android.widget.LinearLayout;

public final class DrawOptionsView extends HorizontalScrollView {

	/**
	 * The width on each side of the buttons used to pick a new color.
	 */
	private static final int COLOR_BUTTON_SIZE = 48;
	
	/**
	 * The width of the space that seperates tools and colors.
	 */
	private static final int SEPERATOR_WIDTH = 20;
	
	/**
	 * The layout that will hold drawing buttons.
	 */
	private LinearLayout layout;
	
	/**
	 * A list of all buttons that select a drawing tool, so that they can be
	 * modified as a group.
	 */
	private List<ImageToggleButton> toolsGroup =
		new ArrayList<ImageToggleButton>();
	
	/**
	 * A list of all buttons that select a color, so that they can be modified
	 * as a group.
	 */
	private List<ImageToggleButton> colorGroup =
		new ArrayList<ImageToggleButton>();
	
	/**
	 * A drawable that will contain the colored pencil image
	 */
	Drawable coloredPencilDrawable;
	
	/**
	 * OnClickListener for when a button representing a color is clicked.
	 * @author Tim Bocek
	 */
	private class ColorListener implements View.OnClickListener {
		/**
		 * The color that this listener will pick when fired.
		 */
		private int mColor;
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
		private int mWidth;
		public StrokeWidthListener(final int width) {
			this.mWidth = width;
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
	 * Listener that is called when different drawing tools are chosen.
	 * @author Tim Bocek
	 */
	public interface OnChangeDrawToolListener {
		/**
		 * Fired when the eraser tool is selected.
		 */
		void onChooseEraser();
		
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
		void onChooseStrokeWidth(int width);
	}
	
	/**
	 * Using the null class pattern; this implementation of
	 * OnChangeDrawToolListener should do nothing.  It is only here to avoid
	 * null checkes whenever one of these methods is called.
	 * @author Tim
	 *
	 */
	public class NullChangeDrawToolListener implements OnChangeDrawToolListener {
		@Override
		public void onChooseEraser() {}

		@Override
		public void onChooseColoredPen(int color) {}

		@Override
		public void onChoosePanTool() {}

		@Override
		public void onChooseStrokeWidth(int width) {}
	}
	
	/**
	 * The listener that is called when something about the current draw tool
	 * changes.
	 */
	private OnChangeDrawToolListener onChangeDrawToolListener =
		new NullChangeDrawToolListener();
	
	/**
	 * Constructs a new DrawOptionsView.
	 * @param context The context to construct in.
	 */
	public DrawOptionsView(final Context context) {
		super(context);
		layout = new LinearLayout(context);
		addView(layout);
		
		coloredPencilDrawable =
			context.getResources().getDrawable(R.drawable.pencilbw);

		final ImageToggleButton panButton = new ImageToggleButton(context);
		panButton.setImageResource(R.drawable.transform_move);
		
		final ImageToggleButton eraserButton = new ImageToggleButton(context);
		eraserButton.setImageResource(R.drawable.eraser);
		
		panButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(final View v) {
				onChangeDrawToolListener.onChoosePanTool();
				untoggleGroup(toolsGroup);
				setGroupVisibility(colorGroup, View.GONE);
				panButton.setToggled(true);
			}
		});
		
		eraserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				onChangeDrawToolListener.onChooseEraser();
				untoggleGroup(toolsGroup);
				setGroupVisibility(colorGroup, View.GONE);
				eraserButton.setToggled(true);
			}
		});
		
		layout.addView(panButton);
		layout.addView(eraserButton);
		toolsGroup.add(panButton);
		toolsGroup.add(eraserButton);
		
		addStrokeWidthButton(2, R.drawable.pencil);
		addStrokeWidthButton(4, R.drawable.pen);
		addStrokeWidthButton(12, R.drawable.paintbrush);
		addStrokeWidthButton(40, R.drawable.inktube);
		
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
	 * Adds a button that selects the given color as the current color.
	 * @param color The color that this button will select.
	 */
	private void addColorButton(final int color) {
		ImageToggleButton b = new ImageToggleButton(this.getContext());
		b.setOnClickListener(new ColorListener(color));
		b.setLayoutParams(new LinearLayout.LayoutParams(
				COLOR_BUTTON_SIZE, COLOR_BUTTON_SIZE));
		b.setImageDrawable(coloredPencilDrawable);
		b.setColorFilter(
				new PorterDuffColorFilter(color, PorterDuff.Mode.OVERLAY));
		layout.addView(b);
		colorGroup.add(b);
	}
	
	/**
	 * Adds a button that changes the stroke width to the given width, and that
	 * uses the given resource ID to represent its self.
	 * @param width The width that this button will change the stroke width to.
	 * @param resourceId ID of the image to draw on this button.
	 */
	private void addStrokeWidthButton(final int width, final int resourceId) {
		ImageToggleButton b = new ImageToggleButton(this.getContext());
		b.setImageResource(resourceId);
		b.setOnClickListener(new StrokeWidthListener(width));
		layout.addView(b);
		toolsGroup.add(b);
	}

	/**
	 * Sets the listener to call when a new draw tool is selected.
	 * @param listener The new listener
	 */
	public void setOnChangeDrawToolListener(
			final OnChangeDrawToolListener listener) {
		this.onChangeDrawToolListener = listener;
	}
	
	/**
	 * Untoggles an entire list of ImageToggleButtons, so we can make sure that
	 * only one button in the list is toggled at once.
	 * @param group The list of ImageToggleButtons to modify.
	 */
	private void untoggleGroup(final List<ImageToggleButton> group) {
		for (ImageToggleButton b : group) {
			b.setToggled(false);
		}
	}
	
	/**
	 * Sets the visibility of a group of buttons.
	 * @param group The list of views to modify.
	 * @param visibility Visibility to pass to each view's setVisibility method.
	 */
	private void setGroupVisibility(final List<ImageToggleButton> group,
			final int visibility) {
		for (ImageToggleButton b : group) {
			b.setVisibility(visibility);
		}
	}
}
