package com.tbocek.android.combatmap;

/**
 * Interface that allows management of a tabbing system without caring whether
 * the tabs are implemented in the ActionBar or in a TabWidget.
 * @author Tim
 *
 */
public abstract class TabManager {
	public abstract void addTab(String text, int mode);
	public abstract void pickTab(int mode);

	private TabSelectedListener mTabSelectedListener;

	public void setTabSelectedListener(TabSelectedListener listener) {
		mTabSelectedListener = listener;
	}

	protected void onTabSelected(int mode) {
		if (mTabSelectedListener != null) {
			mTabSelectedListener.onTabSelected(mode);
		}
	}

	public interface TabSelectedListener {
		void onTabSelected(int tab);
	}
}
