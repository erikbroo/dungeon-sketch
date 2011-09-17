package com.tbocek.android.combatmap;

import java.util.HashMap;

import android.view.View;
import android.widget.TabHost;

public class TabHostTabManager extends TabManager {
	private TabHost tabHost;
	private View content;

	public TabHostTabManager(TabHost tabHost, View content) {
		this.tabHost = tabHost;
		this.content = content;
	}

	@Override
	public void addTab(String text, int mode) {
		TabHost.TabSpec spec = tabHost.newTabSpec(text).setIndicator(text).setContent(new TabHost.TabContentFactory() {

			@Override
			public View createTabContent(String arg0) {
				return content;
			}
		});
		tabHost.addTab(spec);
		tagToMode.put(text, mode);
		modeToTag.put(mode, text);
	}
	HashMap<String, Integer> tagToMode = new HashMap<String, Integer>();
	HashMap<Integer, String> modeToTag = new HashMap<Integer, String>();

	@Override
	public void pickTab(int mode) {
		tabHost.setCurrentTabByTag(modeToTag.get(mode));
	}

}
