package com.tbocek.android.combatmap.view;

import java.util.ArrayList;
import java.util.List;

public class ToggleButtonGroup {
	private List<ImageToggleButton> members = new ArrayList<ImageToggleButton>();

	public void add(ImageToggleButton toAdd) {
		members.add(toAdd);
	}
	
	public void remove(ImageToggleButton toRemove) {
		members.remove(toRemove);
	}
	
	public void setGroupVisibility(final int visibility) {
        for (ImageToggleButton b : members) {
            b.setVisibility(visibility);
        }
    }

	public void untoggle() {
        for (ImageToggleButton b : members) {
            b.setToggled(false);
        }
	}
}
