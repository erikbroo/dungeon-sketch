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
	
	List<Button> colorButtons = new ArrayList<Button>();
	List<Integer> colors = new ArrayList<Integer>();
	
	private View.OnClickListener onColorButtonClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			for (int i = 0; i < colorButtons.size(); ++i) {
				if (colorButtons.get(i) == v) {
					if (onChangeDrawToolListener != null) {
						onChangeDrawToolListener.onChooseColoredPen(colors.get(i));
					}					
				}
			}
			
		}
		
	};
	
	public interface OnChangeDrawToolListener {
		void onChooseEraser();
		void onChooseColoredPen(int color);
		void onChoosePanTool();
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
	}
	
	private void addColorButton(String name, int color) {
		colors.add(color);
		Button b = new Button(this.getContext());
		b.setText(name + " Pencil");
		b.setOnClickListener(onColorButtonClickListener);
		colorButtons.add(b);
		layout.addView(b);
	}

	public void setOnChangeDrawToolListener(OnChangeDrawToolListener onChangeDrawToolListener) {
		this.onChangeDrawToolListener = onChangeDrawToolListener;
	}


}
