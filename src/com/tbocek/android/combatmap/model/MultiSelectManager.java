package com.tbocek.android.combatmap.model;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tbocek.android.combatmap.model.primitives.BaseToken;

/**
 * This class tracks a selection of multiple tokens in a way that each token
 * button can contribute to and make use of the selections.
 * @author Tim Bocek
 *
 */
public final class MultiSelectManager {
	/**
	 * The selected tokens.  Maps original object hash code to token (the
	 * default token implementation hashes on TokenID, which isn't good enough
	 * for us).
	 */
	private Map<Integer, BaseToken> mSelection = Maps.newHashMap();

	/**
	 * The callback to use when the selection is modified.
	 */
	private SelectionChangedListener mSelectionChangedListener;
	
	/**
	 * Changes the callback to use when the selection changes.
	 * @param listener The new listener.
	 */
	public void setSelectionChangedListener(SelectionChangedListener listener) {
		mSelectionChangedListener = listener;
	}
	
	/**
	 * Adds a token to the current selection.
	 * @param t The token to add.  Should be a unique clone.
	 */
	public void addToken(final BaseToken t) {
		if (mSelection.isEmpty() && mSelectionChangedListener != null) {
			mSelectionChangedListener.selectionStarted();
		}
		mSelection.put(System.identityHashCode(t), t);
		t.setSelected(true);
		if (mSelectionChangedListener != null) {
			mSelectionChangedListener.selectionChanged();
		}
	}

	/**
	 * Removes the token with the given ID from the current selection.
	 * @param token The token to remove.
	 */
	public void removeToken(final BaseToken token) {
		token.setSelected(false);
		mSelection.remove(System.identityHashCode(token));
		if (mSelectionChangedListener != null) {
			mSelectionChangedListener.selectionChanged();
			if (mSelection.isEmpty()) {
				mSelectionChangedListener.selectionEnded();
			}
		}
	}
	
	/**
	 * Toggles the selected state of the given token.
	 * @param token The token to toggle.
	 */
	public void toggleToken(BaseToken token) {
		if (token.isSelected()) {
			removeToken(token);
		} else {
			addToken(token);
		}
	}

	/**
	 * Returns the selected tokens, in no particular order.
	 * @return The tokens.
	 */
	public Collection<BaseToken> getSelectedTokens() {
		return mSelection.values();
	}

	/**
	 * Clears the selection.
	 */
	public void selectNone() {
		for (BaseToken t: mSelection.values()) {
			t.setSelected(false);
		}
		mSelection.clear();
		if (mSelectionChangedListener != null) {
			mSelectionChangedListener.selectionEnded();
		}
	}
	
	/**
	 * Callback to define actions to take when a selection has changed.
	 * @author Tim
	 *
	 */
	public interface SelectionChangedListener {
		
		/**
		 * Called when a new selection is started.
		 */
		void selectionStarted();
		
		/**
		 * Called when a selection is cleared.
		 */
		void selectionEnded();
		
		/**
		 * Called when a single token is added or removed from the collection.
		 */
		void selectionChanged();
	}
}
