package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.BuiltInImageToken;
import com.tbocek.android.combatmap.graphicscore.CustomBitmapToken;
import com.tbocek.android.combatmap.graphicscore.LetterToken;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;
import com.tbocek.android.combatmap.DataManager;
import com.tbocek.android.combatmap.R;

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

	public void reloadAllTokens() {
		tokenLayout.removeAllViews();
		loadCustomImageTokens();
		loadBuiltInImageTokens();
		loadColorTokens();
		loadLetterTokens();
	}

	private void loadCustomImageTokens() {
		DataManager dataManager = new DataManager(this.getContext());
		CustomBitmapToken.dataManager = dataManager;
		for (String filename : dataManager.tokenFiles()) {
			addTokenPrototype(new CustomBitmapToken(filename));
		}
	}
	
	private void loadBuiltInImageTokens() {
		addTokenPrototype(new BuiltInImageToken(R.drawable.dragongirl_dragontigernight));
		addTokenPrototype(new BuiltInImageToken(R.drawable.orc_libmed));
		addTokenPrototype(new BuiltInImageToken(R.drawable.orc2_libmed));
	}

	private void loadColorTokens() {
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, 1, 1};
			addTokenPrototype( new SolidColorToken(Color.HSVToColor(hsv)));
		}
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, .5f, 1};
			addTokenPrototype( new SolidColorToken(Color.HSVToColor(hsv)));
		}
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, 1, .5f};
			addTokenPrototype( new SolidColorToken(Color.HSVToColor(hsv)));
		}
		
		addTokenPrototype( new SolidColorToken(Color.WHITE) );
		addTokenPrototype(  new SolidColorToken(Color.LTGRAY) );
		addTokenPrototype( new SolidColorToken(Color.GRAY) );
		addTokenPrototype( new SolidColorToken(Color.DKGRAY) );
		addTokenPrototype( new SolidColorToken(Color.BLACK) );
	}

	private void loadLetterTokens() {
		addTokenPrototype(new LetterToken("A"));
		addTokenPrototype(new LetterToken("B"));
		addTokenPrototype(new LetterToken("C"));
		addTokenPrototype(new LetterToken("D"));
		addTokenPrototype(new LetterToken("E"));
		addTokenPrototype(new LetterToken("F"));
		addTokenPrototype(new LetterToken("G"));
		addTokenPrototype(new LetterToken("H"));
		addTokenPrototype(new LetterToken("I"));
		addTokenPrototype(new LetterToken("J"));
		addTokenPrototype(new LetterToken("K"));
		addTokenPrototype(new LetterToken("L"));
		addTokenPrototype(new LetterToken("M"));
		addTokenPrototype(new LetterToken("N"));
		addTokenPrototype(new LetterToken("O"));
		addTokenPrototype(new LetterToken("P"));
		addTokenPrototype(new LetterToken("Q"));
		addTokenPrototype(new LetterToken("R"));
		addTokenPrototype(new LetterToken("S"));
		addTokenPrototype(new LetterToken("T"));
		addTokenPrototype(new LetterToken("U"));
		addTokenPrototype(new LetterToken("V"));
		addTokenPrototype(new LetterToken("W"));
		addTokenPrototype(new LetterToken("X"));
		addTokenPrototype(new LetterToken("Y"));
		addTokenPrototype(new LetterToken("Z"));
	}
}
