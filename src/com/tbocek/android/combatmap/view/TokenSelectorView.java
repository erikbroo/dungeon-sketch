package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class TokenSelectorView extends HorizontalScrollView {
	LinearLayout tokenLayout;
	
	public TokenSelectorView(Context context) {
		super(context);
		
		//Create and add the child layout, which will be a linear layout of tokens.
		tokenLayout = new LinearLayout(context);
		addView(tokenLayout);
		
		addTokenPrototype( new SolidColorToken(Color.BLUE));
		addTokenPrototype( new SolidColorToken(Color.GREEN));
		addTokenPrototype( new SolidColorToken(Color.BLACK));
		addTokenPrototype( new SolidColorToken(Color.RED));
		addTokenPrototype( new SolidColorToken(Color.CYAN));
		addTokenPrototype( new SolidColorToken(Color.GRAY));
		addTokenPrototype( new SolidColorToken(Color.DKGRAY));
		addTokenPrototype( new SolidColorToken(Color.YELLOW));
		addTokenPrototype( new SolidColorToken(Color.WHITE));
		addTokenPrototype( new SolidColorToken(Color.MAGENTA));
		
		addTokenPrototype( new SolidColorToken(Color.BLUE));
		addTokenPrototype( new SolidColorToken(Color.GREEN));
		addTokenPrototype( new SolidColorToken(Color.BLACK));
		addTokenPrototype( new SolidColorToken(Color.RED));
		addTokenPrototype( new SolidColorToken(Color.CYAN));
		addTokenPrototype( new SolidColorToken(Color.GRAY));
		addTokenPrototype( new SolidColorToken(Color.DKGRAY));
		addTokenPrototype( new SolidColorToken(Color.YELLOW));
		addTokenPrototype( new SolidColorToken(Color.WHITE));
		addTokenPrototype( new SolidColorToken(Color.MAGENTA));
		addTokenPrototype( new SolidColorToken(Color.BLUE));
		addTokenPrototype( new SolidColorToken(Color.GREEN));
		addTokenPrototype( new SolidColorToken(Color.BLACK));
		addTokenPrototype( new SolidColorToken(Color.RED));
		addTokenPrototype( new SolidColorToken(Color.CYAN));
		addTokenPrototype( new SolidColorToken(Color.GRAY));
		addTokenPrototype( new SolidColorToken(Color.DKGRAY));
		addTokenPrototype( new SolidColorToken(Color.YELLOW));
		addTokenPrototype( new SolidColorToken(Color.WHITE));
		addTokenPrototype( new SolidColorToken(Color.MAGENTA));
		addTokenPrototype( new SolidColorToken(Color.BLUE));
		addTokenPrototype( new SolidColorToken(Color.GREEN));
		addTokenPrototype( new SolidColorToken(Color.BLACK));
		addTokenPrototype( new SolidColorToken(Color.RED));
		addTokenPrototype( new SolidColorToken(Color.CYAN));
		addTokenPrototype( new SolidColorToken(Color.GRAY));
		addTokenPrototype( new SolidColorToken(Color.DKGRAY));
		addTokenPrototype( new SolidColorToken(Color.YELLOW));
		addTokenPrototype( new SolidColorToken(Color.WHITE));
		addTokenPrototype( new SolidColorToken(Color.MAGENTA));
	}
	
	public void addTokenPrototype(BaseToken prototype) {
		TokenButton b = new TokenButton(this.getContext(), prototype);
		b.setOnClickListener(onClickListener);
		tokenLayout.addView(b);
	}
	
	private View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			TokenButton clicked = (TokenButton) v;
			if (onTokenSelectedListener != null)
				onTokenSelectedListener.onTokenSelected(clicked.getClone());
		}
	};
	
	public interface OnTokenSelectedListener {
		void onTokenSelected(BaseToken t);
	}
	
	OnTokenSelectedListener onTokenSelectedListener = null;

	public void setOnTokenSelectedListener(OnTokenSelectedListener onTokenSelectedListener) {
		this.onTokenSelectedListener = onTokenSelectedListener;
	}
}
