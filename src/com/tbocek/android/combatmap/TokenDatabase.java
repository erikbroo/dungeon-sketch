package com.tbocek.android.combatmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
import android.widget.Toast;

import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;
import com.tbocek.android.combatmap.model.primitives.CustomBitmapToken;
import com.tbocek.android.combatmap.model.primitives.LetterToken;
import com.tbocek.android.combatmap.model.primitives.SolidColorToken;
import com.tbocek.android.combatmap.model.primitives.Util;


/**
 * Provides a lightweight database storing a list of tokens and allowing them
 * to be associated with tags.
 * @author Tim
 *
 */
public final class TokenDatabase {
    /**
     * Mapping from a string representing a token ID to a set of tags that that
     * token has.
     */
    private Map<String, Set<String>> tokensForTag
            = new HashMap<String, Set<String>>();

    /**
     * Mapping from a string representing a tag to a set of token IDs that have
     * that tag.
     */
    private Map<String, Set<String>> tagsForToken
            = new HashMap<String, Set<String>>();

    /**
     * Mapping from a Token ID to an instantiated token object that has that ID.
     */
    private transient Map<String, BaseToken> tokenForId
            = new HashMap<String, BaseToken>();

    /**
     * Whether tags need to be pre-populated during the loading step.  By
     * default we want this to be true (so that tokens added to the library
     * get the right tags), but we want to be able to suppress it while batch
     * loading tokens.
     */
    private transient boolean prePopulateTags = true;

	/**
	 * Always-present member at the top of the tag list that selects all tokens.
	 */
	public static final String ALL = "All";

    /**
     * Delimiter to use when saving the token database.
     */
    private static final String FILE_DELIMITER = "`";

    /**
     * Private constructor - this is a singleton.
     */
    private TokenDatabase() { }

    /**
     * The singleton token database instance.
     */
    private static TokenDatabase instance;

    /**
     * Returns the instance of the token database.
     * @param context A context to use when loading data if needed.
     * @return The token database.
     */
    public static TokenDatabase getInstance(final Context context) {
        if (instance == null) {
            try {
                instance = TokenDatabase.load(context);
            } catch (Exception e) {
                instance = new TokenDatabase();
                instance.populate(new DataManager(context));
            }
        }
        return instance;
    }

    /**
     * Adds a tag with no tokens associated with it to the token database.
     * @param tag The tag to add.
     */
    public void addEmptyTag(final String tag) {
        tokensForTag.put(tag, new HashSet<String>());
    }

    /**
     * Tags the token with the given collection of tags.
     * @param tokenId ID of the token to tag.
     * @param tags Tags to add to the token.
     */
    public void tagToken(final String tokenId, final Collection<String> tags) {
        for (String tag : tags) {
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

    /**
     * Tags the token with the given collection of tags.
     * @param token The token object to tag.
     * @param tags Tags to add to the token.
     */
    public void tagToken(final BaseToken token, final Set<String> tags) {
        tagToken(token.getTokenId(), tags);
    }

    /**
     * Tags the token with the given single tag.
     * @param tokenId The ID of the token object to tag.
     * @param tag The tag to add.
     */
    public void tagToken(final String tokenId, final String tag) {
        Set<String> tags = new HashSet<String>();
        tags.add(tag);
        tagToken(tokenId, tags);

    }

    /**
     * Gets a list of all tags in the token collection, sorted alphabetically
     * and case-insensitively.
     * @return The sorted tags.
     */
    public Collection<String> getTags() {
        ArrayList<String> l = new ArrayList<String>(tokensForTag.keySet());
        Collections.sort(l, new Comparator<String>() {
            @Override
            public int compare(final String s1, final String s2) {
                return s1.toUpperCase().compareTo(s2.toUpperCase());
            }
        });
        return l;
    }

    /**
     * Gets all tokens in the collection, sorted by ID.
     * @return The tokens.
     */
    public List<BaseToken> getAllTokens() {
    	return tokenIdsToTokens(tokenForId.keySet());
    }

    /**
     * Given a collection of tags, returns a sorted list of all tokens that have
     * at least one of those tags.
     * @param tags The tags to look for.
     * @return The tokens for those tags.
     */
    public List<BaseToken> tokensForTags(final Collection<String> tags) {
        Set<String> tokenIds = new HashSet<String>();
        for (String tag : tags) {
            tokenIds.addAll(this.tokensForTag.get(tag));
        }
        return tokenIdsToTokens(tokenIds);
    }

    /**
     * Given a tag, returns a sorted list of all tokens that have that tag.
     * @param tag The tag to look for.
     * @return The tokens associated with the requested tag.
     */
    public List<BaseToken> getTokensForTag(final String tag) {
    	if (tag == ALL) {
    		return this.getAllTokens();
    	}
    	Set<String> tokenIds = this.tokensForTag.get(tag);
    	return tokenIdsToTokens(tokenIds);
    }

	/**
	 * Given a collection of token IDs, returns a list of tokens, sorted by
	 * ID, that that contains the known tokens that match those IDs.
	 * @param tokenIds Collection of IDs to look up.
	 * @return List of tokens.
	 */
	private List<BaseToken> tokenIdsToTokens(
			final Collection<String> tokenIds) {
		ArrayList<String> sortedIds = new ArrayList<String>(tokenIds);
        Collections.sort(sortedIds);

        List<BaseToken> tokens = new ArrayList<BaseToken>();

        for (String tokenId : sortedIds) {
            // Add the token for this ID.
            // No worries if the token doesn't exist - by design the database
            // could include tokens that don't exist anymore since it connects a
        	// loaded token id to stored information about that ID.
            if (tokenForId.containsKey(tokenId)) {
                tokens.add(tokenForId.get(tokenId));
            }
        }
        return tokens;
	}

    /**
     * Adds a token and tags it with default tags.
     * @param token The token to add and tag.
     */
    public void addTokenPrototype(final BaseToken token) {
        tokenForId.put(token.getTokenId(), token);
        if (this.prePopulateTags) {
        	tagToken(token, token.getDefaultTags());
        }
    }

    /**
     * Removes the given tag from the given token.  It is not an error to remove
     * a tag that is not on the token in the first place.
     * @param tokenId ID of the token to remove the tag from.
     * @param tag The tag to remove.
     */
    public void removeTagFromToken(final String tokenId, final String tag) {
        this.tagsForToken.get(tokenId).remove(tag);
        this.tokensForTag.get(tag).remove(tokenId);

    }

    /**
     * Removes a token from the database.  This does nothing to ensure that the
     * token will not be re-added next time the database populates.
     * @param token The token to remove.
     */
    public void removeToken(final BaseToken token) {
        for (String tag : tagsForToken.get(token.getTokenId())) {
            tokensForTag.get(tag).remove(token.getTokenId());
        }
        tagsForToken.remove(token.getTokenId());
        tokenForId.remove(token.getTokenId());
    }

    /**
     * Populates the token database with built-in tokens and custom tokens
     * loaded from the token manager.
     * @param dataManager The data manager to load tokens from.
     */
    public void populate(final DataManager dataManager) {
        this.prePopulateTags = !tagsLoaded();
    	loadCustomImageTokens(dataManager);
        loadBuiltInImageTokens();
        loadColorTokens();
        loadLetterTokens();
        this.prePopulateTags = false;
    }
    
    public BaseToken createToken(String tokenId) {
    	return this.tokenForId.get(tokenId).clone();
    }

    /**
     * @return True if tags have already been loaded from the file, false if
     * 		they need to be pre-populated.
     */
    private boolean tagsLoaded() {
    	return this.tokensForTag.size() != 0;
	}

	/**
     * Searches for tokens that display images from the custom image directory
     * on the SD card and adds them to the database.
     * @param dataManager The data manager to use when searching for tokens.
     */
    private void loadCustomImageTokens(final DataManager dataManager) {
        CustomBitmapToken.registerDataManager(dataManager);
        for (String filename : dataManager.tokenFiles()) {
            addTokenPrototype(new CustomBitmapToken(filename));
        }
    }

    /**
     * Populates the token database with built-in image tokens.
     */
    private void loadBuiltInImageTokens() {
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
        addBuiltin(R.drawable.perseus);
        addBuiltin(R.drawable.gorgon);
        addBuiltin(R.drawable.dragongirl_dragontigernight);
        addBuiltin(R.drawable.oaken_man);
        addBuiltin(R.drawable.flagron);
        addBuiltin(R.drawable.goren);
        addBuiltin(R.drawable.vampire_soldier);
        addBuiltin(R.drawable.veranesse);
        addBuiltin(R.drawable.algrim);
        addBuiltin(R.drawable.aquatic_evil);
        addBuiltin(R.drawable.avalanche);
        addBuiltin(R.drawable.baltar);
        addBuiltin(R.drawable.berwyn);
        addBuiltin(R.drawable.black_daggers);
        addBuiltin(R.drawable.black_duke);
        addBuiltin(R.drawable.circelo);
        addBuiltin(R.drawable.col_blinky);
        addBuiltin(R.drawable.conall);
        addBuiltin(R.drawable.cullin);
        addBuiltin(R.drawable.damisk);
        addBuiltin(R.drawable.dehna);
        addBuiltin(R.drawable.dirk_centipede);
        addBuiltin(R.drawable.dragon_throne);
        addBuiltin(R.drawable.dr_ranger);
        addBuiltin(R.drawable.elvira);
        addBuiltin(R.drawable.el_vago_ill);
        addBuiltin(R.drawable.e_mist);
        addBuiltin(R.drawable.fawn);
        addBuiltin(R.drawable.ferdinand);
        addBuiltin(R.drawable.flamebeard);
        addBuiltin(R.drawable.gerymn);
        addBuiltin(R.drawable.glimnor);
        addBuiltin(R.drawable.green_dragon);
        addBuiltin(R.drawable.gyrlang);
        addBuiltin(R.drawable.halfskull);
        addBuiltin(R.drawable.heretic);
        addBuiltin(R.drawable.herugrim);
        addBuiltin(R.drawable.icebarb);
        addBuiltin(R.drawable.icebarb2);
        addBuiltin(R.drawable.ikomalo);
        addBuiltin(R.drawable.isobelle);
        addBuiltin(R.drawable.kal);
        addBuiltin(R.drawable.kateri);
        addBuiltin(R.drawable.ken_invoke);
        addBuiltin(R.drawable.lady_dread);
        addBuiltin(R.drawable.lioness);
        addBuiltin(R.drawable.lohman);
        addBuiltin(R.drawable.luloah);
        addBuiltin(R.drawable.magian);
        addBuiltin(R.drawable.martial);
        addBuiltin(R.drawable.melvs);
        addBuiltin(R.drawable.minotaur);
        addBuiltin(R.drawable.mostin);
        addBuiltin(R.drawable.mpillar);
        addBuiltin(R.drawable.musashi);
        addBuiltin(R.drawable.necro);
        addBuiltin(R.drawable.nereid);
        addBuiltin(R.drawable.nimbus);
        addBuiltin(R.drawable.ogre);
        addBuiltin(R.drawable.owlbear);
        addBuiltin(R.drawable.pax);
        addBuiltin(R.drawable.penny);
        addBuiltin(R.drawable.pieter);
        addBuiltin(R.drawable.puma);
        addBuiltin(R.drawable.rauce);
        addBuiltin(R.drawable.reilo);
        addBuiltin(R.drawable.sag);
        addBuiltin(R.drawable.scrollo);
        addBuiltin(R.drawable.seraph);
        addBuiltin(R.drawable.shark_thing);
        addBuiltin(R.drawable.sister_eden);
        addBuiltin(R.drawable.smilodon);
        addBuiltin(R.drawable.snow);
        addBuiltin(R.drawable.stjya);
        addBuiltin(R.drawable.tai);
        addBuiltin(R.drawable.terma);
        addBuiltin(R.drawable.thargad);
        addBuiltin(R.drawable.thorn);
        addBuiltin(R.drawable.urolka);
        addBuiltin(R.drawable.vesten);
        addBuiltin(R.drawable.void_walker);
        addBuiltin(R.drawable.zanth);

    }

    /**
     * Adds a built-in image token with the given resource ID to the token
     * database.
     * @param resourceId The ID of the drawable resource to add.
     */
    private void addBuiltin(final int resourceId) {
        addTokenPrototype(new BuiltInImageToken(resourceId));
    }

    /**
     * Populates the database with solid-colored tokens.
     */
    private void loadColorTokens() {
    	int sortOrder = 0;
        for (int color : Util.getStandardColorPalette()) {
            addTokenPrototype(new SolidColorToken(color, sortOrder));
            sortOrder += 1;
        }
    }

    /**
     * Populates the database with letter-in-a-circle tokens.
     */
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

    /**
     * Saves the token database to internal storage.
     * @param context Context to use when saving the database.
     * @throws IOException on write error.
     */
    public void save(final Context context) throws IOException {
        FileOutputStream output =
                context.openFileOutput("token_database", Context.MODE_PRIVATE);
        BufferedWriter dataOut =
                new BufferedWriter(new OutputStreamWriter(output));
        save(dataOut);
        dataOut.close();
    }

    /**
     * Writes the token database to the given writer.
     * @param output The writer to write the token database to.
     * @throws IOException on write error.
     */
    private void save(final BufferedWriter output) throws IOException {
        for (String tokenName : this.tagsForToken.keySet()) {
            output.write(tokenName);
            output.write(FILE_DELIMITER);
            for (String tag : this.tagsForToken.get(tokenName)) {
                output.write(tag);
                output.write(FILE_DELIMITER);
            }
            output.newLine();
        }
    }

    /**
     * Loads a token database from internal storage, and replaces the current
     * singleton token database with the loaded one.
     * @param context The context to use when loading.
     * @return The loaded database.
     * @throws IOException On read error.
     */
    public static TokenDatabase load(final Context context)
    		throws IOException {
        TokenDatabase d = new TokenDatabase();
        d.populate(new DataManager(context));

        FileInputStream input = context.openFileInput("token_database");
        BufferedReader dataIn =
        	new BufferedReader(new InputStreamReader(input));
        d.load(dataIn);
        dataIn.close();

        return d;
    }

    /**
     * Reads data into this token database from the given reader.
     * @param dataIn Reader to get input from.
     * @throws IOException On read error.
     */
    private void load(final BufferedReader dataIn) throws IOException {
        String line;
        while ((line = dataIn.readLine()) != null) {
            String[] tokens = line.split(FILE_DELIMITER);
            String tokenId = tokens[0];
            ArrayList<String> tags = new ArrayList<String>();
            for (int i = 1; i < tokens.length; ++i) {
                tags.add(tokens[i]);
            }
            this.tagToken(tokenId, tags);
        }
    }

}
