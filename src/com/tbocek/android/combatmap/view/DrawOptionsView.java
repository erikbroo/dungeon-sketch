package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.R;
import com.tbocek.android.combatmap.graphicscore.Util;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DrawOptionsView extends HorizontalScrollView {

	LinearLayout layout;
	
	List<ImageToggleButton> toolsGroup = new ArrayList<ImageToggleButton>();
	List<ImageToggleButton> colorGroup = new ArrayList<ImageToggleButton>();
	
	private class ColorListener implements View.OnClickListener {
		private int color;
		public ColorListener(int color) {
			this.color = color;
		}
		
		@Override
		public void onClick(View v) {
			onChangeDrawToolListener.onChooseColoredPen(color);
			untoggleGroup(colorGroup);
			((ImageToggleButton)v).setToggled(true);
		}
	}
	
	private class StrokeWidthListener implements View.OnClickListener {
		private int width;
		public StrokeWidthListener(int width) {
			this.width = width;
		}
		
		@Override
		public void onClick(View v) {
			onChangeDrawToolListener.onChooseStrokeWidth(width);	
			untoggleGroup(toolsGroup);
			setGroupVisibility(colorGroup, View.VISIBLE);
			((ImageToggleButton)v).setToggled(true);
		}
	}
	
	
	public interface OnChangeDrawToolListener {
		void onChooseEraser();
		void onChooseColoredPen(int color);
		void onChoosePanTool();
		void onChooseStrokeWidth(int width);
	}
	
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
	
	private OnChangeDrawToolListener onChangeDrawToolListener = new NullChangeDrawToolListener();
	
	public DrawOptionsView(Context context) {
		super(context);
		layout = new LinearLayout(context);
		addView(layout);
		

		final ImageToggleButton panButton = new ImageToggleButton(context);
		panButton.setImageResource(R.drawable.transform_move);
		
		final ImageToggleButton eraserButton = new ImageToggleButton(context);
		eraserButton.setImageResource(R.drawable.eraser);
		
		panButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				onChangeDrawToolListener.onChoosePanTool();
				untoggleGroup(toolsGroup);
				setGroupVisibility(colorGroup, View.GONE);
				panButton.setToggled(true);
			}
		});
		
		eraserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
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
		seperator.setLayoutParams(new LinearLayout.LayoutParams(20, LinearLayout.LayoutParams.MATCH_PARENT));
		layout.addView(seperator);
		
		for (int color : Util.getStandardColorPalette()) {
			addColorButton(color);
		}
	}
	
	private void addColorButton(int color) {
		PencilButton b = new PencilButton(this.getContext(), color);
		b.setOnClickListener(new ColorListener(color));
		b.setLayoutParams(new LinearLayout.LayoutParams(48,48));
		layout.addView(b);
		colorGroup.add(b);
	}
	
	private void addStrokeWidthButton(int width, int resourceId) {
		ImageToggleButton b = new ImageToggleButton(this.getContext());
		b.setImageResource(resourceId);
		b.setOnClickListener(new StrokeWidthListener(width));
		layout.addView(b);
		toolsGroup.add(b);
	}

	public void setOnChangeDrawToolListener(OnChangeDrawToolListener onChangeDrawToolListener) {
		this.onChangeDrawToolListener = onChangeDrawToolListener;
	}
	
	private void untoggleGroup(List<ImageToggleButton> group) {
		for (ImageToggleButton b:group) {
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
