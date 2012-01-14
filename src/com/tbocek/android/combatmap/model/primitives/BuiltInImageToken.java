package com.tbocek.android.combatmap.model.primitives;

import java.text.DecimalFormat;
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
	 * Format string that pads the sort order with 0s.
	 */
	private static final DecimalFormat SORT_ORDER_FORMAT =
		new DecimalFormat("#0000.###");
	
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
     * Relative order to sort this token in.
     */
    private int mSortOrder;
    
    /**
     * Tags loaded for this built-in token.
     */
    private Set<String> mDefaultTags;

    /**
     * Constructor from resource ID.
     * @param resourceId The resource to load for this token.
     * @param sortOrder Integer that will be used to specify a sort order for
     * 		this class.
     */
    public BuiltInImageToken(final int resourceId, final int sortOrder,
    		final Set<String> defaultTags) {
        mResourceId = resourceId;
        mSortOrder = sortOrder;
        mDefaultTags = defaultTags;
    }
    
    @Override
    protected Drawable createDrawable() {
        return res != null ? res.getDrawable(mResourceId) : null;
    }

    @Override
    public BaseToken clone() {
        return copyAttributesTo(new BuiltInImageToken(
        		mResourceId, mSortOrder, mDefaultTags));
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
        s.addAll(mDefaultTags);
        return s;
    }
    
    @Override
    protected String getTokenClassSpecificSortOrder() {
    	SORT_ORDER_FORMAT.setDecimalSeparatorAlwaysShown(false);
        return SORT_ORDER_FORMAT.format(mSortOrder);

    }
}
