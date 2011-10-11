package com.tbocek.android.combatmap;

import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A tab manager that adds tabs as buttons to a layout.
 * @author Tim
 *
 */
public class LegacyTabManager extends TabManager {

	ViewGroup mContainer;

	public LegacyTabManager(ViewGroup container) {
		mContainer = container;
	}

	@Override
	public void addTab(final String text, final int mode) {
		Button b = new Button(mContainer.getContext());
		b.setText(text);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				onTabSelected(mode);
			}

		});
		mContainer.addView(b);
	}

	@Override
	public void pickTab(int mode) {
		this.onTabSelected(mode);
	}

}
