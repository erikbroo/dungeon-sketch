package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a group of toggle buttons that should be treated as a group,
 * providing methods to make sure that only one at a time is selected.
 * @author Tim
 *
 */
public class ToggleButtonGroup {
	/**
	 * Toggle buttons that make up the group.
	 */
	private List<ImageToggleButton> mMembers 
			= new ArrayList<ImageToggleButton>();
	
	/**
	 * The toggle button that should be selected when the default is requested.
	 */
	private ImageToggleButton mDefaultMember;
	
	/**
	 * Adds a toggle button to the group.  Toggle buttons may belong to multiple
	 * groups, though this could cause problems.
	 * @param toAdd The toggle button to add.
	 */
	public void add(ImageToggleButton toAdd) {
		mMembers.add(toAdd);
		if (mDefaultMember == null) {
			mDefaultMember = toAdd;
		}
	}
	
	/**
	 * Removes the given toggle button from the group.
	 * @param toRemove The toggle button to remove.
	 */
	public void remove(ImageToggleButton toRemove) {
		mMembers.remove(toRemove);
		if (mDefaultMember == toRemove) {
			mDefaultMember = !mMembers.isEmpty() ? mMembers.get(0) : null;
		}
	}
	
	/**
	 * Shows or hides all toggle buttons as a group.
	 * @param visibility One of the visibility codes in View to apply to the
	 * 		entire group.
	 */
	public void setGroupVisibility(final int visibility) {
        for (ImageToggleButton b : mMembers) {
            b.setVisibility(visibility);
        }
    }
	
	/**
	 * Select the default button if no button is already selected.  If a button
	 * is selected, uses that as the default, but clicks it anyway to ensure
	 * that a sane state is reached.
	 */
	public void maybeSelectDefault() {
		for (ImageToggleButton b : mMembers) {
			if (b.isToggled()) {
				b.performClick();
				return;
			}
		}
		mDefaultMember.performClick();
	}
	
	/**
	 * Untoggles all buttons in the group.  Normally done before a new one is
	 * toggled.
	 */
	public void untoggle() {
        for (ImageToggleButton b : mMembers) {
            b.setToggled(false);
        }
	}

	/**
	 * Forces the default option to become the currently toggled option,
	 * regardless of whether something else was toggled before.
	 */
	public void forceDefault() {
		untoggle();
		mDefaultMember.performClick();
	}
}
