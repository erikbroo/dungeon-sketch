package com.tbocek.android.combatmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import android.widget.Toast;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.BuiltInImageToken;
import com.tbocek.android.combatmap.graphicscore.CustomBitmapToken;
import com.tbocek.android.combatmap.graphicscore.LetterToken;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;
import com.tbocek.android.combatmap.graphicscore.Util;


public final class TokenDatabase implements Serializable {
    /**
     * Mapping from a string representing a token ID to a set of tags that that
     * token has.
     */
    public Map<String, Set<String>> tokensForTag
            = new HashMap<String, Set<String>>();
            
    /**
     * Mapping from a string representing a tag to a set of token IDs that have
     * that tag.
     */
    public Map<String, Set<String>> tagsForToken
            = new HashMap<String, Set<String>>();
            
    /**
     * Mapping from a Token ID to an instantiated token object that has that ID.
     */
    public transient Map<String, BaseToken> tokenForId
            = new HashMap<String, BaseToken>();

    /**
     * Delimiter to use when saving the token database.
     */
    private static final String FILE_DELIMITER="`";

    /**
     * Private constructor - this is a singleton.
     */
    private TokenDatabase() {}

    /**
     * The singleton token database instance.
     */
    private static TokenDatabase instance;

    /**
     * Returns the instance of the token database.
     * @param context A context to use when loading data if needed.
     * @return The token database.
     */
    public static TokenDatabase getInstance(Context context) {
        if (instance == null) {
            try {
                instance = TokenDatabase.load(context);
            } catch (Exception e) {
                instance = new TokenDatabase();
                instance.populate(new DataManager(context));
                Toast t = Toast.makeText(context, 
                        "Could not open the token database: " + e.toString(), 
                        Toast.LENGTH_LONG);
            }
        }
        return instance;
    }

    /**
     * Adds a token to the database, signaling its availability to link to a 
     * token ID.  There may be tokens represented in the database that are not 
     * attached to an actual token.  These are not considered valid.
     * @param token The token to add.
     */
    public void addToken(BaseToken token) {
        tokenForId.put(token.getTokenId(), token);
    }
    
    /**
     * Adds a tag with no tokens associated with it to the token database.
     * @param tag The tag to add.
     */
    public void addEmptyTag(String tag) {
        tokensForTag.put(tag, new HashSet<String>());
    }
    
    /**
     * Tags the token with the given collection of tags.
     * @param tokenId ID of the token to tag.
     * @param tags Tags to add to the token.
     */
    public void tagToken(String tokenId, Collection<String> tags) {
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

    /**
     * Tags the token with the given collection of tags.
     * @param token  The token object to tag.
     * @param tags Tags to add to the token.
     */
    public void tagToken(BaseToken token, Set<String> tags) {
        tagToken(token.getTokenId(), tags);
    }

    /**
     * Tags the token with the given single tag.
     * @param tokenID The ID of the token object to tag.
     * @param tag The tag to add.
     */
    public void tagToken(String tokenId, String tag) {
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
            public int compare(String s1, String s2) {
                return s1.toUpperCase().compareTo(s2.toUpperCase());
            }
        });
        return l;
    }
    
    /**
     * Gets all tokens in the collection, sorted by ID.
     */
    public Collection<BaseToken> getAllTokens() {
        //TODO: De-dupe
        ArrayList<String> sortedIds = 
            new ArrayList<String>(tokenForId.keySet());
        Collections.sort(sortedIds);

        List<BaseToken> tokens = new ArrayList<BaseToken>();

        for(String tokenId : sortedIds) {
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
     * Given a collection of tags, returns a sorted list of all tokens that have
     * at least one of those tags.
     * @param tags The tags to look for.
     */
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
 
    /**
     * Given a tag, returns a sorted list of all tokens that have that tag.
     * @param tag The tag to look for.
     */
    public List<BaseToken> getTokensForTag(String tag) {
        ArrayList<String> tags = new ArrayList<String>();
        tags.add(tag);
        return tokensForTags(tags);
    }
 
    /**
     * Adds a token and tags it with default tags.
     * TODO: is addToken needed?
     * @param token The token to add and tag.
     */
    private void addTokenPrototype(BaseToken token) {
        addToken(token);
        tagToken(token, token.getDefaultTags());
    }
    
    /**
     * Removes the given tag from the given token.  It is not an error to remove
     * a tag that is not on the token in the first place.
     * @param tokenId ID of the token to remove the tag from.
     * @param tag The tag to remove.
     */
    public void removeTagFromToken(String tokenId, String tag) {
        this.tagsForToken.get(tokenId).remove(tag);
        this.tokensForTag.get(tag).remove(tokenId);

    }
    
    /**
     * Removes a token from the database.  This does nothing to ensure that the
     * token will not be re-added next time the database populates.
     * @param token The token to remove.
     */
    public void removeToken(BaseToken token) {
        for (String tag : tagsForToken.get(token.getTokenId())) {
            tokensForTag.get(tag).remove(token.getTokenId());
        }
        tagsForToken.remove(token.getTokenId());
        tokenForId.remove(token.getTokenId());
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
    
    /**
     * Searches for tokens that display images from the custom image directory
     * on the SD card and adds them to the database.
     * @param dataManager The data manager to use when searching for tokens.
     */
    private void loadCustomImageTokens(DataManager dataManager) {
        CustomBitmapToken.registerDataManager(dataManager);
        for (String filename : dataManager.tokenFiles()) {
            addTokenPrototype(new CustomBitmapToken(filename));
        }
    }

    /**
     * Populates the token database with built-in image tokens.
     */
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

    /**
     * Adds a built-in image token with the given resource ID to the token
     * database.
     */
    private void addBuiltin(int resourceId) {
        addTokenPrototype(new BuiltInImageToken(resourceId));
    }

    /**
     * Populates the database with solid-colored tokens.
     */
    private void loadColorTokens() {
        for (int color : Util.getStandardColorPalette()) {
            addTokenPrototype(new SolidColorToken(color));
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
    public void save(Context context) throws IOException {
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
    private void save(BufferedWriter output) throws IOException {
        for (String tokenName : this.tagsForToken.keySet()) {
            output.write(tokenName);
            output.write(FILE_DELIMITER);
            for(String tag : this.tagsForToken.get(tokenName)) {
                output.write(tag);
                output.write(FILE_DELIMITER);
            }
            output.newLine();
        }
    }

    public static TokenDatabase load(Context context) throws IOException, ClassNotFoundException {
        TokenDatabase d = new TokenDatabase();
        d.populate(new DataManager(context));

        FileInputStream input = context.openFileInput("token_database");
        BufferedReader dataIn = new BufferedReader(new InputStreamReader(input));
        d.load(dataIn);
        dataIn.close();

        return d;
    }

    private void load(BufferedReader dataIn) throws IOException {
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
