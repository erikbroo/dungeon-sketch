package com.tbocek.android.combatmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;
import com.tbocek.android.combatmap.model.primitives.CustomBitmapToken;
import com.tbocek.android.combatmap.model.primitives.LetterToken;
import com.tbocek.android.combatmap.model.primitives.PlaceholderToken;
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
     * Delimiter to use when saving the token database.
     */
    private static final String FILE_DELIMITER = "`";

	/**
	 * Always-present member at the top of the tag list that selects all tokens.
	 */
	public static final String ALL = "All";
    
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
                instance.populate(context);
            }
        }
        return instance;
    }
    
    
    /**
     * Returns an instance of the token database, or null if it hasn't been
     * created.
     * @return The token database.
     */
    public static TokenDatabase getInstanceOrNull() {
    	return instance;
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
        d.populate(context);
        
        FileInputStream input = context.openFileInput("token_database");
        BufferedReader dataIn =
        	new BufferedReader(new InputStreamReader(input));
        d.load(dataIn);
        dataIn.close();
        
        d.removeDeletedBuiltins();

        return d;
    }

	/**
     * Mapping from a string representing a token ID to a set of tags that that
     * token has.
     */
    private Map<String, Set<String>> mTokensForTag
            = new HashMap<String, Set<String>>();

    /**
     * Mapping from a string representing a tag to a set of token IDs that have
     * that tag.
     */
    private Map<String, Set<String>> mTagsForToken = Maps.newHashMap();

    /**
     * Mapping from a Token ID to an instantiated token object that has that ID.
     */
    private transient Map<String, BaseToken> mTokenForId = Maps.newHashMap();
    
    /**
     * Built in tokens that have been deleted.
     */
    private Set<String> mDeletedBuiltInTokens = Sets.newHashSet();
    
    /**
     * Mapping from deprecated token IDs to their replacements.
     */
    private Map<String, String> mOldIdMapping = Maps.newHashMap();

    /**
     * Whether tags need to be pre-populated during the loading step.  By
     * default we want this to be true (so that tokens added to the library
     * get the right tags), but we want to be able to suppress it while batch
     * loading tokens.
     */
    private transient boolean mPrePopulateTags = true;



    /**
     * Private constructor - this is a singleton.
     */
    private TokenDatabase() { }



    /**
     * Adds a tag with no tokens associated with it to the token database.
     * @param tag The tag to add.
     */
    public void addEmptyTag(final String tag) {
        mTokensForTag.put(tag, new HashSet<String>());
    }

    /**
     * Tags the token with the given collection of tags.
     * @param tokenId ID of the token to tag.
     * @param tags Tags to add to the token.
     */
    public void tagToken(String tokenId, final Collection<String> tags) {
    	tokenId = this.getNonDeprecatedTokenId(tokenId);
        for (String tag : tags) {
            if (!mTokensForTag.containsKey(tag)) {
                mTokensForTag.put(tag, new HashSet<String>());
            }
            mTokensForTag.get(tag).add(tokenId);
        }

        if (!mTagsForToken.containsKey(tokenId)) {
            mTagsForToken.put(tokenId, new HashSet<String>());
        }
        mTagsForToken.get(tokenId).addAll(tags);
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
    public List<String> getTags() {
        ArrayList<String> l = new ArrayList<String>(mTokensForTag.keySet());
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
    	return tokenIdsToTokens(mTokenForId.keySet());
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
            tokenIds.addAll(this.mTokensForTag.get(tag));
        }
        return tokenIdsToTokens(tokenIds);
    }

    /**
     * Given a tag, returns a sorted list of all tokens that have that tag.
     * @param tag The tag to look for.
     * @return The tokens associated with the requested tag.
     */
    public List<BaseToken> getTokensForTag(final String tag) {
    	if (tag.equals(ALL)) {
    		return this.getAllTokens();
    	}
    	Set<String> tokenIds = this.mTokensForTag.get(tag);
    	return tokenIdsToTokens(tokenIds);
    }

	/**
	 * Given a collection of token IDs, returns a list of tokens, sorted based
	 * on the sort order that each token class defines, that that contains the 
	 * known tokens that match those IDs.
	 * @param tokenIds Collection of IDs to look up.
	 * @return List of tokens.
	 */
	private List<BaseToken> tokenIdsToTokens(
			final Collection<String> tokenIds) {

        List<BaseToken> tokens = new ArrayList<BaseToken>();

        for (String tokenId : tokenIds) {
        	tokenId = this.getNonDeprecatedTokenId(tokenId);
            // Add the token for this ID.
            // No worries if the token doesn't exist - by design the database
            // could include tokens that don't exist anymore since it connects a
        	// loaded token id to stored information about that ID.
            if (mTokenForId.containsKey(tokenId)) {
                tokens.add(mTokenForId.get(tokenId));
            }
        }
        
        Collections.sort(tokens, new Comparator<BaseToken>() {
			@Override
			public int compare(BaseToken t1, BaseToken t2) {
				// TODO Auto-generated method stub
				return t1.getSortOrder().compareTo(t2.getSortOrder());
			}
        });
        return tokens;
	}

    /**
     * Adds a token and tags it with default tags.
     * @param token The token to add and tag.
     */
    public void addTokenPrototype(final BaseToken token) {
        mTokenForId.put(token.getTokenId(), token);
        if (this.mPrePopulateTags) {
        	tagToken(token, token.getDefaultTags());
        }
    }

    /**
     * Removes the given tag from the given token.  It is not an error to remove
     * a tag that is not on the token in the first place.
     * @param tokenId ID of the token to remove the tag from.
     * @param tag The tag to remove.
     */
    public void removeTagFromToken(String tokenId, final String tag) {
    	tokenId = this.getNonDeprecatedTokenId(tokenId);
        this.mTagsForToken.get(tokenId).remove(tag);
        this.mTokensForTag.get(tag).remove(tokenId);

    }

    /**
     * Removes a token from the database.  This does nothing to ensure that the
     * token will not be re-added next time the database populates.
     * @param token The token to remove.
     */
    public void removeToken(final BaseToken token) {
        for (String tag : mTagsForToken.get(token.getTokenId())) {
            mTokensForTag.get(tag).remove(token.getTokenId());
        }
        mTagsForToken.remove(token.getTokenId());
        mTokenForId.remove(token.getTokenId());
        if (token.isBuiltIn()) {
        	mDeletedBuiltInTokens.add(token.getTokenId());
        }
    }

    /**
     * Populates the token database with built-in tokens and custom tokens
     * loaded from the token manager.
     * @param context 
     */
    public void populate(Context context) {
        this.mPrePopulateTags = !tagsLoaded();
    	loadCustomImageTokens(new DataManager(context));
        loadBuiltInImageTokens(context);
        loadColorTokens();
        loadLetterTokens();
        this.mPrePopulateTags = false;
    }
    
    /**
     * Creates a new token given the given token ID.
     * @param tokenId TokenID to create a token for.
     * @return A new token cloned from the prototype for that Token ID.
     */
    public BaseToken createToken(String tokenId) {
    	tokenId = this.getNonDeprecatedTokenId(tokenId);
    	BaseToken prototype = this.mTokenForId.get(tokenId);
    	if (prototype == null) {
    		Log.e("TokenDatabase", "Token did not exist for ID: " + tokenId);
    		return new PlaceholderToken(tokenId);
    	}
    	return prototype.clone();
    }

    /**
     * @return True if tags have already been loaded from the file, false if
     * 		they need to be pre-populated.
     */
    private boolean tagsLoaded() {
    	return this.mTokensForTag.size() != 0;
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
     * Populates the token database with built-in image tokens by loading the
     * list of token resource names from the art credits.
     * @param context Context to load resources from.
     */
    private void loadBuiltInImageTokens(Context context) {
        try {
        	InputStream is = context.getResources().openRawResource(
        			R.raw.art_credits);
        	SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(new ArtCreditHandler(context));
			xr.parse(new InputSource(is));
			is.close();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * SAX handler to load the resources that represent built-in tokens from the
     * art credits file.
     * @author Tim
     *
     */
    private class ArtCreditHandler extends DefaultHandler {
    	/**
    	 * Application context to load resources from.
    	 */
		private Context mContext;
		
		/**
		 * Count of the tokens that have been loaded; used to sort them later.
		 */
		private int mCurrentSortOrder;
		
		/**
		 * Constructor.
		 * @param context Application context to load resources from.
		 */
		public ArtCreditHandler(Context context) {
			this.mContext = context;
		}
		
		@Override
		public void startElement(
				String namespaceURI, String localName, String qName, 
		        org.xml.sax.Attributes atts) throws SAXException {
			if (localName.equalsIgnoreCase("token")) {
				int id = mContext.getResources().getIdentifier(
						atts.getValue("res"), 
						"drawable", 
						"com.tbocek.android.combatmap");
				String tagList = atts.getValue("tags");
				Set<String> defaultTags = Sets.newHashSet();
				if (tagList != null) {
					for (String s: tagList.split(",")) {
						defaultTags.add(s);
					}
				}
				addBuiltin(
						atts.getValue("res"), id, mCurrentSortOrder, 
						defaultTags);
				mCurrentSortOrder++;
			}
		} 
	}

    /**
     * Adds a built-in image token with the given resource ID to the token
     * database.
     * @param resourceName The name of the drawable resource to add.
     * @param resourceId The ID of the drawable resource to add.
     * @param sortOrder The sort order to use. 
     * @param defaultTags Tags that this built in token should be in by default.
     */
    private void addBuiltin(
    		final String resourceName, final int resourceId, 
    		final int sortOrder, Set<String> defaultTags) {
    	BuiltInImageToken t = new BuiltInImageToken(
        		resourceName, sortOrder, defaultTags); 
        addTokenPrototype(t);
        mapOldId(t.getTokenId(), 
        		"BuiltInImageToken" + Integer.toString(resourceId));
    }

    /**
     * Adds a note that the given old ID is deprecated in favor of the given
     * new ID.
     * @param newId The new ID.
     * @param oldId The deprecated ID.
     */
    private void mapOldId(String newId, String oldId) {
		mOldIdMapping.put(oldId, newId);
	}
    
    /**
     * Gets a TokenId, dereferencing a deprecated ID into the new ID if 
     * necessary.
     * @param tokenId The ID to possibly dereference.
     * @return The dereferenced TokenID.
     */
    private String getNonDeprecatedTokenId(String tokenId) {
    	if (mOldIdMapping.containsKey(tokenId)) {
    		return mOldIdMapping.get(tokenId);
    	}
    	return tokenId;
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
    	// Start by writing a single line containing the deleted custom tokens
    	output.write("DELETED_TOKENS");
    	output.write(FILE_DELIMITER);
    	for (String tokenName: this.mDeletedBuiltInTokens) {
    		output.write(tokenName);
    		output.write(FILE_DELIMITER);
    	}
    	output.newLine();
    	
    	// Write tags for each token.
        for (String tokenName : this.mTagsForToken.keySet()) {
            output.write(tokenName);
            output.write(FILE_DELIMITER);
            for (String tag : this.mTagsForToken.get(tokenName)) {
                output.write(tag);
                output.write(FILE_DELIMITER);
            }
            output.newLine();
        }
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
            if (tokenId.equals("DELETED_TOKENS")) {
            	for (int i = 1; i < tokens.length; ++i) {
            		mDeletedBuiltInTokens.add(
            				getNonDeprecatedTokenId(tokens[i]));
            	}
            } else {
                ArrayList<String> tags = new ArrayList<String>();
                for (int i = 1; i < tokens.length; ++i) {
                    tags.add(tokens[i]);
                }
                this.tagToken(tokenId, tags);
            }
        }
    }
    
    /**
     * Remove all built-in tokens that the user has previously deleted.
     */
    private void removeDeletedBuiltins() {
		for (String removedBuiltin: mDeletedBuiltInTokens) {
			this.mTokenForId.remove(removedBuiltin);
		}
	}


    /**
     * Deletes the given tag from the database.
     * @param toDelete The tag to remove from the database.
     */
	public void deleteTag(String toDelete) {
		this.mTokensForTag.remove(toDelete);
		for (String tokenId: mTagsForToken.keySet()) {
			mTagsForToken.get(tokenId).remove(toDelete);
		}
	}
}
