package com.tbocek.android.combatmap.graphicscore;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Creates a token for one of the built-in images.
 * @author Tim
 *
 */
public class BuiltInImageToken extends DrawableToken {
	/**
	 * HACK: The resources.  This must be set prior to creating BuildInImageTokens.
	 */
	public static transient Resources res;
	
	private int mResourceId;
	
	public BuiltInImageToken(int resourceId) {
		mResourceId = resourceId;
	}
	
	protected Drawable createDrawable() {
		return res != null ? res.getDrawable(mResourceId) : null;
	}
	
	@Override
	public BaseToken clone() {
		return new BuiltInImageToken(mResourceId);
	}

	@Override
	protected String getTokenClassSpecificId() {
		return Integer.toString(mResourceId);
	}
	
	public Set<String> getDefaultTags() {
		Set<String> s = new HashSet<String>();
		s.add("built-in");
		s.add("image");
		return s;
	}
}
