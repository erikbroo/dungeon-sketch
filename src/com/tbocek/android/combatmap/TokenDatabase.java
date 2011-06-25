package com.tbocek.android.combatmap;

import com.tbocek.android.combatmap.graphicscore.BaseToken;

public class TokenDatabase {
	/**
	 * Adds a token to the database, signalling its availability to link to a token ID.
	 * There may be tokens represented in the database that are not attached to an actual token.
	 * These are not considered valid.
	 * @param token
	 */
	public void addToken(BaseToken token) {
		
	}
	
	public void addTagToToken(BaseToken token, String tag) {
		addTagToToken(token.getTokenId(), tag);
	}
	
	public void addTagToToken(String tokenId, String tag) {
		
	}
	
	public void removeTagFromToken(BaseToken token, String tag) {
		removeTagFromToken(token.getTokenId(), tag);
	}
	
	public void removeTagFromToken(String tokenId, String tag) {
		
	}
	
	public void removeToken(BaseToken token) {
		removeToken(token.getTokenId());
	}
	
	public void removeToken(String tokenId) {
	
	}
}
