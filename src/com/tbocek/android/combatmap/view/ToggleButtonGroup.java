package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

public class ToggleButtonGroup {
	private List<ImageToggleButton> mMembers 
			= new ArrayList<ImageToggleButton>();
	private ImageToggleButton mDefaultMember;
	
	public void add(ImageToggleButton toAdd) {
		mMembers.add(toAdd);
		if (mDefaultMember == null) {
			mDefaultMember = toAdd;
		}
	}
	
	public void remove(ImageToggleButton toRemove) {
		mMembers.remove(toRemove);
		if (mDefaultMember == toRemove) {
			mDefaultMember = !mMembers.isEmpty() ? mMembers.get(0) : null;
		}
	}
	
	public void setGroupVisibility(final int visibility) {
        for (ImageToggleButton b : mMembers) {
            b.setVisibility(visibility);
        }
    }
	
	public void maybeSelectDefault() {
		for (ImageToggleButton b : mMembers) {
			if (b.isToggled()) {
				return;
			}
		}
		mDefaultMember.performClick();
	}
	
	public void untoggle() {
        for (ImageToggleButton b : mMembers) {
            b.setToggled(false);
        }
	}

	public void forceDefault() {
		untoggle();
		mDefaultMember.performClick();
	}
}
