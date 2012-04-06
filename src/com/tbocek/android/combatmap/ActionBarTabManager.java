package com.tbocek.android.combatmap;

import java.util.HashMap;
import java.util.Map;

import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;


/**
 * Tab manager implementation that uses tabs natively shown in Honeycomb and
 * later's action bar.
 * @author Tim
 *
 */
public class ActionBarTabManager extends TabManager {
	
	/**
	 * Action bar that provides the tabs.
	 */
	private ActionBar mActionBar;

    /**
     * Reverse lookup so we know what tab to select when forced into an
     * interaction mode.
     */
    private Map<Integer, ActionBar.Tab> mManipulationModeTabs =
    	new HashMap<Integer, ActionBar.Tab>();

    /**
     * Constructor.
     * @param actionBar The action bar that will provide the tabs.
     */
	public ActionBarTabManager(ActionBar actionBar) {
		this.mActionBar = actionBar;
	}

	@Override
	public void addTab(final String description, final int mode) {
    	ActionBar.Tab tab = mActionBar.newTab();
    	tab.setText(description);
    	tab.setTabListener(new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
				
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
    	mActionBar.addTab(tab);
    	mManipulationModeTabs.put(mode, tab);
	}

	@Override
	public void pickTab(int mode) {
        mManipulationModeTabs.get(mode).select();
	}

}
