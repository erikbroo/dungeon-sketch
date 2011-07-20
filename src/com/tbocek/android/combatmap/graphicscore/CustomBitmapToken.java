package com.tbocek.android.combatmap.graphicscore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.tbocek.android.combatmap.DataManager;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * TODO(tbocek): Make these share a drawable!
 * @author Tim
 *
 */
public final class CustomBitmapToken extends DrawableToken {
    public static transient DataManager dataManager = null;

    private String filename = null;

    public CustomBitmapToken(String filename) {
        this.filename = filename;
    }

    @Override
    protected Drawable createDrawable() {
        if (dataManager == null) return null;
        Bitmap b;
        try {
            b = dataManager.loadTokenImage(filename);
            return new BitmapDrawable(b);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public BaseToken clone() {
        return new CustomBitmapToken(filename);
    }

    @Override
    protected String getTokenClassSpecificId() {
        return filename;
    }

    public Set<String> getDefaultTags() {
        Set<String> s = new HashSet<String>();
        s.add("custom");
        s.add("image");
        return s;
    }

    @Override
    public boolean maybeDeletePermanently() throws IOException {
        dataManager.deleteTokenImage(filename);
        return true;
    }

    @Override
    public boolean isBuiltIn() {
        return false;
    }
}
