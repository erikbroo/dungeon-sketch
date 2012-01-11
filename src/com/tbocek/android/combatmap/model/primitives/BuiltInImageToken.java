package com.tbocek.android.combatmap.model.primitives;

import java.util.HashSet;
import java.util.Set;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Creates a token for one of the built-in images.
 * @author Tim Bocek
 *
 */
public final class BuiltInImageToken extends DrawableToken {

    /**
     * HACK: The resources.  This must be set prior to creating
     * BuildInImageTokens.
     */
    private static transient Resources res;

    /**
     * Sets the resources that images will be loaded from.
     * @param resources The resources object.
     */
    public static void registerResources(final Resources resources) {
        res = resources;
    }

    /**
     * The resource to load for this token.
     */
    private int mResourceId;

    /**
     * Constructor from resource ID.
     * @param resourceId The resource to load for this token.
     */
    public BuiltInImageToken(final int resourceId) {
        mResourceId = resourceId;
    }
    
    @Override
    protected Drawable createDrawable() {
        return res != null ? res.getDrawable(mResourceId) : null;
    }

    @Override
    public BaseToken clone() {
        return copyAttributesTo(new BuiltInImageToken(mResourceId));
    }

    @Override
    protected String getTokenClassSpecificId() {
        return Integer.toString(mResourceId);
    }

    @Override
    public Set<String> getDefaultTags() {
        Set<String> s = new HashSet<String>();
        s.add("built-in");
        s.add("image");
        return s;
    }
}
