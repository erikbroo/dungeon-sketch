package com.tbocek.android.combatmap;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;

/**
 * Interface that allows management of a tabbing system without caring whether
 * the tabs are implemented in the ActionBar or in a TabWidget.  Tab actions
 * are forwarded to a listener regardless of the tab implementation.
 * @author Tim
 *
 */
public class TabManager {
	
	/**
     * Constructor.
     * @param actionBar The action bar that will provide the tabs.
     */
	public TabManager(ActionBar actionBar, Context context) {
		this.mActionBar = actionBar;
		this.mContext = context;
	}

	/**
	 * Listener that fires when a tab is selected.
	 */
	private TabSelectedListener mTabSelectedListener;
	
	private int mLastSelectedMode = -1;
	
	private HashMap<Integer, Boolean> modesForGm = new HashMap<Integer, Boolean>();

	/**
	 * Action bar that provides the tabs.
	 */
	protected ActionBar mActionBar;

	/**
	 * Reverse lookup so we know what tab to select when forced into an
	 * interaction mode.
	 */
	private Map<Integer, ActionBar.Tab> mManipulationModeTabs = new HashMap<Integer, ActionBar.Tab>();

	protected Context mContext;
	
	public final void addTab(String description, final int mode, boolean forGm) {
		ActionBar.Tab tab = mActionBar.newTab();
		tab.setText(description);
		tab.setTabListener(new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
				
			}
	
			@Override
			public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
				TabManager.this.onTabSelected(mode);
			}
	
			@Override
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
				// TODO Auto-generated method stub
	
			}
		});
		mActionBar.addTab(tab);
		mManipulationModeTabs.put(mode, tab);
		modesForGm.put(mode, forGm);
	}
	
	/**
	 * Sets the listener that implements the tab selection action.
	 * @param listener The new tab selected listener.
	 */
	public void setTabSelectedListener(TabSelectedListener listener) {
		mTabSelectedListener = listener;
	}

	/**
	 * Subclasses can call this to fire the tab selected listener.
	 * @param mode The integer identifier for the mode that was selected.
	 */
	protected void onTabSelected(int mode) {
		if (needGmScreenConfirmation(mode)) {
			confirmGmScreenBeforeSwitchingTabs(mode);
		} else {  
			if (mTabSelectedListener != null) {
				mTabSelectedListener.onTabSelected(mode);
			}
			mLastSelectedMode = mode;
		}
	}
	
	protected boolean needGmScreenConfirmation(int mode) {
		if (mLastSelectedMode == -1) {return false;} // Do not need confirmation for first selection.
		if (!PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("gmscreen", false)) {return false;}
		return this.modesForGm.get(mode).booleanValue() && !this.modesForGm.get(this.mLastSelectedMode).booleanValue();
	}

	public void pickTab(int mode) {
		mManipulationModeTabs.get(mode).select();
		mLastSelectedMode = mode;
	}

	private void confirmGmScreenBeforeSwitchingTabs(final int destinationMode) {
		int switchBackMode = TabManager.this.mLastSelectedMode;
		TabManager.this.mLastSelectedMode = -1;
		mManipulationModeTabs.get(switchBackMode).select();
		
		new AlertDialog.Builder(this.mContext)
			.setCancelable(true)
			.setMessage(R.string.gm_screen_spoiler_warning)
			.setPositiveButton(R.string.gm_screen_mistchief_managed, new OnClickListener() {
		
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					TabManager.this.mLastSelectedMode = -1; // Make sure we don't get kicked into the dialog again!
					mManipulationModeTabs.get(destinationMode).select();
					
				}
				
			})
			.setNegativeButton(R.string.gm_screen_cancel, new OnClickListener() {
		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			})
			.create().show();
	}

	/**
	 * Listener interface for when a new tab is selected.
	 * @author Tim
	 *
	 */
	public interface TabSelectedListener {
		/**
		 * Fires when a new tab is selected.
		 * @param tab Integer identifier for the tab that was selected.
		 */
		void onTabSelected(int tab);
	}
}
