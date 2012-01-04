package com.tbocek.android.combatmap.tokenmanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.tbocek.android.combatmap.model.primitives.BaseToken;

/**
 * This class tracks a selection of multiple tokens in a way that each token
 * button can contribute to and make use of the selections.
 * @author Tim Bocek
 *
 */
public final class MultiSelectManager {
	/**
	 * The selected tokens.  Maps token ID to token.
	 */
	private Map<String, BaseToken> selection = new HashMap<String, BaseToken>();

	/**
	 * Adds a token to the current selection.
	 * @param t The token to add.  Should be a unique clone.
	 */
	public void addToken(final BaseToken t) {
		selection.put(t.getTokenId(), t);
	}

	/**
	 * Removes the token with the given ID from the current selection.
	 * @param tokenId ID of the token to remove.
	 */
	public void removeToken(final String tokenId) {
		selection.remove(tokenId);
	}

	/**
	 * Returns the selected tokens, in no particular order.
	 * @return The tokens.
	 */
	public Collection<BaseToken> getSelectedTokens() {
		return selection.values();
	}

	/**
	 * Checks whether a token is selected.
	 * @param tokenId Token ID to check.
	 * @return Whether the token ID is currently selected.
	 */
	public boolean isTokenSelected(final String tokenId) {
		return selection.containsKey(tokenId);
	}

	/**
	 * Clears the selection.
	 */
	public void selectNone() {
		selection.clear();
	}
}
