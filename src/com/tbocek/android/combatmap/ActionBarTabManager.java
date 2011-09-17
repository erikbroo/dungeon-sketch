package com.tbocek.android.combatmap;

import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;

public class ActionBarTabManager extends TabManager {
	private ActionBar actionBar;

    /**
     * Reverse lookup so we know what tab to select when forced into an
     * interaction mode.
     */
    private Map<Integer, ActionBar.Tab> manipulationModeTabs =
    	new HashMap<Integer, ActionBar.Tab>();

	public ActionBarTabManager(ActionBar actionBar) {
		actionBar = actionBar;
	}

	@Override
	public void addTab(final String description, final int mode) {
    	ActionBar.Tab tab = actionBar.newTab();
    	tab.setText(description);
    	tab.setTabListener(new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
				ActionBarTabManager.this.onTabSelected(mode);
			}

			@Override
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
				// TODO Auto-generated method stub

			}
    	});
    	actionBar.addTab(tab);
    	manipulationModeTabs.put(mode, tab);
	}

	@Override
	public void pickTab(int mode) {
        manipulationModeTabs.get(mode).select();
	}

}
