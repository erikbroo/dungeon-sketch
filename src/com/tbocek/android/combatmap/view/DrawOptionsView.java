package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.Util;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class DrawOptionsView extends HorizontalScrollView {

	LinearLayout layout;
	
	private class ColorListener implements View.OnClickListener {
		private int color;
		public ColorListener(int color) {
			this.color = color;
		}
		
		@Override
		public void onClick(View v) {
			onChangeDrawToolListener.onChooseColoredPen(color);
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
		

		Button panButton = new Button(context);
		panButton.setText("Pan");
		
		Button eraserButton = new Button(context);
		eraserButton.setText("Erase");
		
		panButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				onChangeDrawToolListener.onChoosePanTool();
			}
		});
		
		eraserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onChangeDrawToolListener.onChooseEraser();
			}
		});
		
		layout.addView(panButton);
		layout.addView(eraserButton);
		
		addStrokeWidthButton(2);
		addStrokeWidthButton(4);
		addStrokeWidthButton(8);
		
		for (int color : Util.getStandardColorPalette()) {
			addColorButton(color);
		}
	}
	
	private void addColorButton(int color) {
		PencilButton b = new PencilButton(this.getContext(), color);
		b.setOnClickListener(new ColorListener(color));
		layout.addView(b);
	}
	
	private void addStrokeWidthButton(int width) {
		Button b = new Button(this.getContext());
		b.setText("Width " + Integer.toString(width));
		b.setOnClickListener(new StrokeWidthListener(width));
		layout.addView(b);
	}

	public void setOnChangeDrawToolListener(OnChangeDrawToolListener onChangeDrawToolListener) {
		this.onChangeDrawToolListener = onChangeDrawToolListener;
	}
}
