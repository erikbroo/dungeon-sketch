package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class DrawOptionsView extends HorizontalScrollView {

	LinearLayout layout;
	
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
		Button pencilButton = new Button(context);
		pencilButton.setText("Draw Black");
		Button redPencilButton = new Button(context);
		redPencilButton.setText("Draw Red");

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
		
		pencilButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				if (onChangeDrawToolListener != null) {
					onChangeDrawToolListener.onChooseColoredPen(Color.BLACK);
				}
				
			}
		});
		
		redPencilButton.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View arg0) {
				if (onChangeDrawToolListener != null) {
					onChangeDrawToolListener.onChooseColoredPen(Color.RED);
				}
				
			}
		});
		
		layout.addView(panButton);
		layout.addView(eraserButton);
		layout.addView(pencilButton);
		layout.addView(redPencilButton);
	}

	public void setOnChangeDrawToolListener(OnChangeDrawToolListener onChangeDrawToolListener) {
		this.onChangeDrawToolListener = onChangeDrawToolListener;
	}


}
