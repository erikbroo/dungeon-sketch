package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

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
			if (onChangeDrawToolListener != null) {
				onChangeDrawToolListener.onChooseColoredPen(color);
			}
		}
	}
	
	private class StrokeWidthListener implements View.OnClickListener {
		private int width;
		public StrokeWidthListener(int width) {
			this.width = width;
		}
		
		@Override
		public void onClick(View v) {
			if (onChangeDrawToolListener != null) {
				onChangeDrawToolListener.onChooseColoredPen(width);
			}
		}
	}
	
	
	public interface OnChangeDrawToolListener {
		void onChooseEraser();
		void onChooseColoredPen(int color);
		void onChoosePanTool();
		void onChooseStrokeWidth(int width);
	}
	private OnChangeDrawToolListener onChangeDrawToolListener = null;
	
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
				if (onChangeDrawToolListener != null) {
					onChangeDrawToolListener.onChoosePanTool();
				}
			}
		});
		
		eraserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onChangeDrawToolListener != null) {
					onChangeDrawToolListener.onChooseEraser();
				}
			}
		});
		
		layout.addView(panButton);
		layout.addView(eraserButton);
		
		addColorButton("Black", Color.BLACK);
		addColorButton("Red", Color.RED);
		addColorButton("Blue", Color.BLUE);
		addColorButton("Green", Color.GREEN);
		addColorButton("Yellow", Color.YELLOW);
		addColorButton("Dark Red", Color.rgb(128, 0, 0));
		addColorButton("Dark Blue", Color.rgb(0, 0, 128));
		addColorButton("Dark Green", Color.rgb(0, 128, 0));
		addStrokeWidthButton(2);
		addStrokeWidthButton(4);
		addStrokeWidthButton(8);
	}
	
	private void addColorButton(String name, int color) {
		Button b = new Button(this.getContext());
		b.setText(name + " Pencil");
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
