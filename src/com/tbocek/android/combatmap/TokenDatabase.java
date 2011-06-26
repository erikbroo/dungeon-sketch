package com.tbocek.android.combatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Color;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.BuiltInImageToken;
import com.tbocek.android.combatmap.graphicscore.CustomBitmapToken;
import com.tbocek.android.combatmap.graphicscore.LetterToken;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;

public class TokenDatabase {
	public Map<String, Set<String>> tokensForTag = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> tagsForToken = new HashMap<String, Set<String>>();
	public Set<String> validTokenIds = new HashSet<String>();
	public Map<String, BaseToken> tokenForId = new HashMap<String, BaseToken>();
	
	/**
	 * Adds a token to the database, signaling its availability to link to a token ID.
	 * There may be tokens represented in the database that are not attached to an actual token.
	 * These are not considered valid.
	 * @param token
	 */
	public void addToken(BaseToken token) {
		tokenForId.put(token.getTokenId(), token);
		validTokenIds.add(token.getTokenId());
	}
	
	public void tagToken(String tokenId, Set<String> tags) {
		validTokenIds.add(tokenId); // We've seen this token ID
		for (String tag: tags) {
			if (!tokensForTag.containsKey(tag)) {
				tokensForTag.put(tag, new HashSet<String>());
			}
			tokensForTag.get(tag).add(tokenId);
		}
		
		if (!tagsForToken.containsKey(tokenId)) {
			tagsForToken.put(tokenId, new HashSet<String>());
		}
		tagsForToken.get(tokenId).addAll(tags);
	}
	
	
	public void tagToken(BaseToken token, Set<String> tags) {
		tagToken(token.getTokenId(), tags);
	}
	

	public Collection<String> getTags() {
		ArrayList<String> l = new ArrayList<String>(tokensForTag.keySet());
		Collections.sort(l);
		return l;
	}
	
	public Collection<BaseToken> getAllTokens() {
		//TODO: De-dupe
		ArrayList<String> sortedIds = new ArrayList<String>(tokenForId.keySet());
		Collections.sort(sortedIds);
		
		List<BaseToken> tokens = new ArrayList<BaseToken>();
		
		for(String tokenId : sortedIds) {
			// Add the token for this ID.
			// No worries if the token doesn't exist - by design the database
			// could include tokens that don't exist anymore since it connects a loaded
			// token id to stored information about that ID.
			if (tokenForId.containsKey(tokenId)) {
				tokens.add(tokenForId.get(tokenId));
			}
		}
		return tokens;
	}
	
	public List<BaseToken> tokensForTags(Collection<String> tags) {
		Set<String> tokenIds = new HashSet<String>();
		for(String tag : tags) {
			tokenIds.addAll(this.tokensForTag.get(tag));
		}
		ArrayList<String> sortedIds = new ArrayList<String>(tokenIds);
		Collections.sort(sortedIds);
		
		List<BaseToken> tokens = new ArrayList<BaseToken>();
		
		for(String tokenId : sortedIds) {
			// Add the token for this ID.
			// No worries if the token doesn't exist - by design the database
			// could include tokens that don't exist anymore since it connects a loaded
			// token id to stored information about that ID.
			if (tokenForId.containsKey(tokenId)) {
				tokens.add(tokenForId.get(tokenId));
			}
		}
		return tokens;
	}
	
	
	private void addTokenPrototype(BaseToken token) {
		addToken(token);
		tagToken(token, token.getDefaultTags());
	}
	
	/**
	 * Populates the token database with built-in tokens and custom tokens loaded
	 * from the token manager.
	 * @param dataManager
	 */
	public void populate(DataManager dataManager) {
		loadCustomImageTokens(dataManager);
		loadBuiltInImageTokens();
		loadColorTokens();
		loadLetterTokens();
	}

	private void loadCustomImageTokens(DataManager dataManager) {
		CustomBitmapToken.dataManager = dataManager;
		for (String filename : dataManager.tokenFiles()) {
			addTokenPrototype(new CustomBitmapToken(filename));
		}
	}


	private void loadBuiltInImageTokens() {
		addTokenPrototype(new BuiltInImageToken(R.drawable.dragongirl_dragontigernight));
		addTokenPrototype(new BuiltInImageToken(R.drawable.orc_libmed));
		addTokenPrototype(new BuiltInImageToken(R.drawable.orc2_libmed));
	}

	private void loadColorTokens() {
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, 1, 1};
			addTokenPrototype( new SolidColorToken(Color.HSVToColor(hsv)));
		}
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, .5f, 1};
			addTokenPrototype( new SolidColorToken(Color.HSVToColor(hsv)));
		}
		
		for (int h = 0; h < 360; h += 30) {
			float [] hsv = {h, 1, .5f};
			addTokenPrototype( new SolidColorToken(Color.HSVToColor(hsv)));
		}
		
		addTokenPrototype( new SolidColorToken(Color.WHITE) );
		addTokenPrototype(  new SolidColorToken(Color.LTGRAY) );
		addTokenPrototype( new SolidColorToken(Color.GRAY) );
		addTokenPrototype( new SolidColorToken(Color.DKGRAY) );
		addTokenPrototype( new SolidColorToken(Color.BLACK) );
	}

	private void loadLetterTokens() {
		addTokenPrototype(new LetterToken("A"));
		addTokenPrototype(new LetterToken("B"));
		addTokenPrototype(new LetterToken("C"));
		addTokenPrototype(new LetterToken("D"));
		addTokenPrototype(new LetterToken("E"));
		addTokenPrototype(new LetterToken("F"));
		addTokenPrototype(new LetterToken("G"));
		addTokenPrototype(new LetterToken("H"));
		addTokenPrototype(new LetterToken("I"));
		addTokenPrototype(new LetterToken("J"));
		addTokenPrototype(new LetterToken("K"));
		addTokenPrototype(new LetterToken("L"));
		addTokenPrototype(new LetterToken("M"));
		addTokenPrototype(new LetterToken("N"));
		addTokenPrototype(new LetterToken("O"));
		addTokenPrototype(new LetterToken("P"));
		addTokenPrototype(new LetterToken("Q"));
		addTokenPrototype(new LetterToken("R"));
		addTokenPrototype(new LetterToken("S"));
		addTokenPrototype(new LetterToken("T"));
		addTokenPrototype(new LetterToken("U"));
		addTokenPrototype(new LetterToken("V"));
		addTokenPrototype(new LetterToken("W"));
		addTokenPrototype(new LetterToken("X"));
		addTokenPrototype(new LetterToken("Y"));
		addTokenPrototype(new LetterToken("Z"));
	}

}
