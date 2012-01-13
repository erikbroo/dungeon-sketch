package com.tbocek.android.combatmap;

/**
 * Interface that allows management of a tabbing system without caring whether
 * the tabs are implemented in the ActionBar or in a TabWidget.  Tab actions
 * are forwarded to a listener regardless of the tab implementation.
 * @author Tim
 *
 */
public abstract class TabManager {

	/**
	 * Listener that fires when a tab is selected.
	 */
	private TabSelectedListener mTabSelectedListener;
	
	/**
	 * Creates a new tab.
	 * @param text Text to display on the tab.
	 * @param mode Integer identifying the mode that the tab represents.
	 */
	public abstract void addTab(String text, int mode);
	
	/**
	 * Forces the given tab to be selected.
	 * @param mode Integer identifier for the mode that should be tabbed to.
	 */
	public abstract void pickTab(int mode);
	
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
		if (mTabSelectedListener != null) {
			mTabSelectedListener.onTabSelected(mode);
		}
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
