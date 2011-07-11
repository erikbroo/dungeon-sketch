package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.TokenDatabase;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class TokenCategorySelector extends ScrollView {
	private LinearLayout innerLayout;
	private TokenDatabase tokenDatabase;
	private OnTagSelectedListener onTagSelectedListener;
	
	public void setOnCheckedListChangedListener(
			OnTagSelectedListener onCheckedListChangedListener) {
		this.onTagSelectedListener = onCheckedListChangedListener;
	}

	public TokenCategorySelector(Context context) {
		super(context);
		innerLayout = new LinearLayout(this.getContext());
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		this.addView(innerLayout);

	}
	
	private void addCheckbox(String text) {
		Button b = new Button(this.getContext());
		b.setText(text);
		b.setTextSize(16);
		innerLayout.addView(b);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (onTagSelectedListener != null) {
					onTagSelectedListener.onTagSelected(((Button)v).getText().toString());
				}
			}
		});
	}

	public void setTokenDatabase(TokenDatabase tokenDatabase) {
		this.tokenDatabase = tokenDatabase;
		innerLayout.removeAllViews();
		for (String tag: tokenDatabase.getTags()) {
			addCheckbox(tag);
		}
	}

	
	public interface OnTagSelectedListener {
		public void onTagSelected(String checkedTag);
	}

}
