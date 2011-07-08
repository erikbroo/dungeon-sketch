package com.tbocek.android.combatmap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Color;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.BuiltInImageToken;
import com.tbocek.android.combatmap.graphicscore.CustomBitmapToken;
import com.tbocek.android.combatmap.graphicscore.LetterToken;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;
import com.tbocek.android.combatmap.graphicscore.Util;

public class TokenDatabase implements Serializable {
	public Map<String, Set<String>> tokensForTag = new HashMap<String, Set<String>>();
	public Map<String, Set<String>> tagsForToken = new HashMap<String, Set<String>>();
	public transient Map<String, BaseToken> tokenForId = new HashMap<String, BaseToken>();
	
	/**
	 * Adds a token to the database, signaling its availability to link to a token ID.
	 * There may be tokens represented in the database that are not attached to an actual token.
	 * These are not considered valid.
	 * @param token
	 */
	public void addToken(BaseToken token) {
		tokenForId.put(token.getTokenId(), token);
	}
	
	public void tagToken(String tokenId, Set<String> tags) {
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
	
	public void addEmptyTag(String tag) {
		tokensForTag.put(tag, new HashSet<String>());
	}
	
	public void tagToken(BaseToken token, Set<String> tags) {
		tagToken(token.getTokenId(), tags);
	}

	public void tagToken(String tokenId, String tag) {
		Set<String> tags = new HashSet<String>();
		tags.add(tag);
		tagToken(tokenId, tags);
		
	}

	public Collection<String> getTags() {
		ArrayList<String> l = new ArrayList<String>(tokensForTag.keySet());
		Collections.sort(l, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.toUpperCase().compareTo(s2.toUpperCase());
			}
		});
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

	public List<BaseToken> getTokensForTag(String newTag) {
		ArrayList<String> tags = new ArrayList<String>();
		tags.add(newTag);
		return tokensForTags(tags);
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
	
	public void save(Context context) throws IOException {
		FileOutputStream output = context.openFileOutput("token_database", Context.MODE_PRIVATE);
		ObjectOutputStream objectOut = new ObjectOutputStream(output);
		objectOut.writeObject(this);
		objectOut.close();
	}
	
	public static TokenDatabase load(Context context) throws IOException, ClassNotFoundException {
		FileInputStream input = context.openFileInput("token_database");
		ObjectInputStream objectIn = new ObjectInputStream(input);
		TokenDatabase d = (TokenDatabase) objectIn.readObject();
		objectIn.close();
		d.tokenForId = new HashMap<String, BaseToken>();
		d.populate(new DataManager(context));
		return d;
	}

	private void loadBuiltInImageTokens() {
		addBuiltin(R.drawable.dragongirl_dragontigernight);
		addBuiltin(R.drawable.orc_libmed);
		addBuiltin(R.drawable.orc2_libmed);
		addBuiltin(R.drawable.cheetah);
		addBuiltin(R.drawable.cougar);
		addBuiltin(R.drawable.cute_horses);
		addBuiltin(R.drawable.dog);
		addBuiltin(R.drawable.dogs);
		addBuiltin(R.drawable.douchy_guy);
		addBuiltin(R.drawable.fox);
		addBuiltin(R.drawable.lamb);
		addBuiltin(R.drawable.lion);
		addBuiltin(R.drawable.pig);
		addBuiltin(R.drawable.rad_stag);
		addBuiltin(R.drawable.stag2);
		addBuiltin(R.drawable.swan);
		addBuiltin(R.drawable.tiger1);
		addBuiltin(R.drawable.tiger2);
	}

	private void addBuiltin(int resourceId) {
		addTokenPrototype(new BuiltInImageToken(resourceId));
	}

	private void loadColorTokens() {
		for (int color : Util.getStandardColorPalette()) {
			addTokenPrototype(new SolidColorToken(color));
		}
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

	public void removeTagFromToken(String tokenId, String tag) {
		this.tagsForToken.get(tokenId).remove(tag);
		this.tokensForTag.get(tag).remove(tokenId);
		
	}

	public void removeToken(BaseToken token) {
		for (String tag : tagsForToken.get(token.getTokenId())) {
			tokensForTag.get(tag).remove(token.getTokenId());
		}
		tagsForToken.remove(token.getTokenId());
		tokenForId.remove(token.getTokenId());
	}


}