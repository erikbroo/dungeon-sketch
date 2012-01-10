package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

public class ToggleButtonGroup {
	private List<ImageToggleButton> members = new ArrayList<ImageToggleButton>();
	private ImageToggleButton defaultMember;
	
	public void add(ImageToggleButton toAdd) {
		members.add(toAdd);
		if (defaultMember == null) {
			defaultMember = toAdd;
		}
	}
	
	public void remove(ImageToggleButton toRemove) {
		members.remove(toRemove);
		if (defaultMember == toRemove) {
			defaultMember = !members.isEmpty() ? members.get(0) : null;
		}
	}
	
	public void setGroupVisibility(final int visibility) {
        for (ImageToggleButton b : members) {
            b.setVisibility(visibility);
        }
    }
	
	public void maybeSelectDefault() {
		for (ImageToggleButton b : members) {
			if (b.isToggled()) {
				return;
			}
		}
		defaultMember.performClick();
	}
	
	public void untoggle() {
        for (ImageToggleButton b : members) {
            b.setToggled(false);
        }
	}

	public void forceDefault() {
		untoggle();
		defaultMember.performClick();
	}
}
