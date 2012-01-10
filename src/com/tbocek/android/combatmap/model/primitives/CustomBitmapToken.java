package com.tbocek.android.combatmap.model.primitives;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.tbocek.android.combatmap.DataManager;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * A token type that is loaded from an external file and displays the image.
 * @author Tim Bocek
 *
 */
public final class CustomBitmapToken extends DrawableToken {

	/**
	 * The data manager that is used to load custom images.
	 */
    private static transient DataManager dataManager = null;

    /**
     * Sets the data manager that will be used to load images.
     * TODO: Can this be combined with the static field in BuiltInImageToken?
     * @param manager The data manager.
     */
	public static void registerDataManager(final DataManager manager) {
		CustomBitmapToken.dataManager = manager;
	}

    /**
     * The filename to load.
     */
    private String mFilename = null;

    /**
     * Constructor.
     * @param filename The filename to load.
     */
    public CustomBitmapToken(final String filename) {
        this.mFilename = filename;
    }

    @Override
    protected Drawable createDrawable() {
        if (dataManager == null) {
			return null;
		}

        Bitmap b;
        try {
            b = dataManager.loadTokenImage(mFilename);
            return new BitmapDrawable(b);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BaseToken clone() {
        return copyAttributesTo(new CustomBitmapToken(mFilename));
    }

    @Override
    protected String getTokenClassSpecificId() {
        return mFilename;
    }

    @Override
    public Set<String> getDefaultTags() {
        Set<String> s = new HashSet<String>();
        s.add("custom");
        s.add("image");
        return s;
    }

    @Override
    public boolean maybeDeletePermanently() throws IOException {
        dataManager.deleteTokenImage(mFilename);
        return true;
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }

}
