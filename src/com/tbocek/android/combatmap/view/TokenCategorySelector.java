package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

import com.tbocek.android.combatmap.TokenDatabase;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class TokenCategorySelector extends ScrollView {
	private LinearLayout innerLayout;
	private TokenDatabase tokenDatabase;
	private List<CheckBox> checkboxes = new ArrayList<CheckBox>();
	private OnCheckedListChangedListener onCheckedListChangedListener;
	
	public void setOnCheckedListChangedListener(
			OnCheckedListChangedListener onCheckedListChangedListener) {
		this.onCheckedListChangedListener = onCheckedListChangedListener;
	}

	public TokenCategorySelector(Context context) {
		super(context);
		innerLayout = new LinearLayout(this.getContext());
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		this.addView(innerLayout);

	}
	
	private void addCheckbox(String text) {
		CheckBox cb = new CheckBox(this.getContext());
		cb.setText(text);
		innerLayout.addView(cb);
		cb.setChecked(true);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (onCheckedListChangedListener != null) {
					onCheckedListChangedListener.onCheckedChanged(getCheckedTags());
				}
			}
		});
		checkboxes.add(cb);
	}

	public void setTokenDatabase(TokenDatabase tokenDatabase) {
		this.tokenDatabase = tokenDatabase;
		innerLayout.removeAllViews();
		checkboxes.clear();
		for (String tag: tokenDatabase.getTags()) {
			addCheckbox(tag);
		}
	}
	
	public List<String> getCheckedTags() {
		List<String> checkedTags = new ArrayList<String>();
		for (CheckBox cb : checkboxes) {
			String DEBUG_text = cb.getText().toString();
			if (cb.isChecked()) {
				checkedTags.add(cb.getText().toString());
			}
		}
		return checkedTags;
	}
	
	public interface OnCheckedListChangedListener {
		public void onCheckedChanged(List<String> checkedTags);
	}

}
