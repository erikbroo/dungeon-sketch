package com.tbocek.android.combatmap.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.tbocek.android.combatmap.TokenDatabase;
import com.tbocek.android.combatmap.model.io.MapDataDeserializer;
import com.tbocek.android.combatmap.model.io.MapDataSerializer;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BoundingRectangle;
import com.tbocek.android.combatmap.model.primitives.CoordinateTransformer;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Util;

import android.graphics.Canvas;


/**
 * Encapsulates a set of tokens that have been placed on the grid.
 * @author Tim Bocek
 *
 */
public final class TokenCollection implements UndoRedoTarget {

    /**
     * The tokens that have been added to the grid.
     */
    private List<BaseToken> mTokens = new ArrayList<BaseToken>();
    
    /**
     * Command history that supports undo and redo of token operations.
     */
    private CommandHistory mCommandHistory;
    
    /**
     * Command that is checkpointed to while modifying a token, so that the
     * state can be saved for undo/redo.
     */
    private transient ModifyTokenCommand mBuildingCommand;
    
    /**
     * Constructor.
     * @param history Command history to use for undo/redo operations.
     */
    public TokenCollection(CommandHistory history) {
    	mCommandHistory = history;
    }
    
    /**
     * Given a point in screen space, returns the token under that point.
     * @param p The point in screen space.
     * @param transformer Grid space to screen space transformation.
     * @return The token under the point, or null if no tokens.
     */
    public BaseToken getTokenUnderPoint(
    		final PointF p, final CoordinateTransformer transformer) {
        for (int i = 0; i < mTokens.size(); ++i) {
        	float distance =
        		Util.distance(
        				p, transformer.worldSpaceToScreenSpace(
        						mTokens.get(i).getLocation()));
            if (distance < transformer.worldSpaceToScreenSpace(
            		mTokens.get(i).getSize() / 2)) {
                return mTokens.get(i);
            }
        }
        return null;
    }

    /**
     * Adds a token to the collection.
     * @param t The token to add.
     */
    public void addToken(final BaseToken t) {
    	AddOrDeleteTokenCommand c = new AddOrDeleteTokenCommand(this);
    	c.addToken(t);
        mCommandHistory.execute(c);
    }

    /**
     * Removes a token from the collection.
     * @param t The token to remove.
     */
    public void remove(final BaseToken t) {
    	AddOrDeleteTokenCommand c = new AddOrDeleteTokenCommand(this);
    	c.deleteToken(t);
        mCommandHistory.execute(c);
    }

    /**
     * Finds a location on the given grid to place this token, and places the
     * token there.  This allows the token to snap to the grid.
     * @param t The token to place.
     * @param attemptedPoint The location where this token should try to be
     * 		placed.
     * @param grid The grid to snap to.
     */
    public void placeTokenNearby(
    		final BaseToken t, final PointF attemptedPoint, final Grid grid) {
        int attemptedDistance = 0;
        PointF point = attemptedPoint;
        // Continually increment the attempted distance until an open grid space
        // is found.  This is guaranteed to succeed. Note that there are some
        // inefficiencies here (the center point is tried four times, each
        // corner of a square is tried twice, etc).  I don't care.  This runs
        // fast enough for reasonable token collections on screen.
        point = grid.getNearestSnapPoint(point, t.getSize());
        while (true) {
            // Go clockwise around the size of a square centered on the
        	// originally attempted point and with sized of
        	// length attemptedDistance*2

            //Across the top
        	// The -attemptedDistance + 1 ensures a nice spiral pattern
            for (int i = -attemptedDistance + 1; i <= attemptedDistance; ++i) {
                if (tryToPlaceHere(t, new PointF(
                		point.x + i, point.y - attemptedDistance))) {
                	return;
                }
            }

            //Down the right
            for (int i = -attemptedDistance; i <= attemptedDistance; ++i) {
                if (tryToPlaceHere(t, new PointF(
                		point.x + attemptedDistance, point.y + i))) {
                	return;
                }
            }

            //Across the bottom
            for (int i = attemptedDistance; i >= -attemptedDistance; --i) {
                if (tryToPlaceHere(t, new PointF(
                		point.x + i, point.y + attemptedDistance))) {
                	return;
                }
            }

            //Up the left
            for (int i = attemptedDistance; i >= -attemptedDistance; --i) {
                if (tryToPlaceHere(t, new PointF(
                		point.x - attemptedDistance, point.y + i))) {
                	return;
                }
            }
            attemptedDistance++;
        }
    }

    /**
     * Tries to place the token at the specified location.  If a token is
     * already here, returns False.  If not, sets the tokens location and
     * returns True.
     * @param t The token to try to place.
     * @param point The location at which to try to place the token.
     * @return True if successfully placed.
     */
    private boolean tryToPlaceHere(final BaseToken t, final PointF point) {
        if (isLocationUnoccupied(point, t.getSize() / 2)) {
            t.setLocation(point);
            return true;
        }
        return false;
    }

    /**
     * Checks whether placing a token with the given radius at the give location
     * would intersect any other tokens.
     * @param point Center of the token to try placing here.
     * @param radius Radius of the token to try placing here.
     * @return True if the location is unoccupied, False if there would be an
     * 		intersection.
     */
    private boolean isLocationUnoccupied(
    		final PointF point, final double radius) {
        for (BaseToken t : mTokens) {
            if (Util.distance(point, t.getLocation())
            		< radius + t.getSize() / 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Draws all tokens.
     * @param canvas The canvas to draw on.
     * @param transformer Transformer from grid space to screen space.
     * @param isDark Whether to draw as if on a dark background.
     * @param isManipulatable Whether tokens can currently be manipulated.
     */
    public void drawAllTokens(
    		final Canvas canvas, final CoordinateTransformer transformer,
    		boolean isDark, boolean isManipulatable) {
        for (int i = 0; i < mTokens.size(); ++i) {
        	// TODO: Take advantage of knowing whether we have a dark
        	// background.
            mTokens.get(i).drawInPosition(canvas, transformer, isDark,
            		isManipulatable);
        }

    }

    /**
     * Computes and returns a bounding rectangle that can contain all the
     * tokens.
     * @return The bounding rectangle.
     */
    public BoundingRectangle getBoundingRectangle() {
    	BoundingRectangle r = new BoundingRectangle();
        for (BaseToken t : mTokens) {
            r.updateBounds(t.getBoundingRectangle());
        }
        return r;
    }

    /**
     * Returns whether there are tokens in this collection.
     * @return True if collection is empty.
     */
    public boolean isEmpty() {
        return mTokens.isEmpty();
    }
    
    /**
     * Sets up this TokenCollection to create a command that modifies the given
     * token.  The current state of this token will be duplicated and saved
     * for undo purposes.
     * @param t The token to checkpoint.
     */
    public void checkpointToken(BaseToken t) {
    	mBuildingCommand = new ModifyTokenCommand(t);
    	mBuildingCommand.checkpointBeforeState();
    }
    
    /**
     * Adds an entry to the command history based on the previously checkpointed
     * token.  The created command will swap the token's state between a copy
     * of the state when this method was called, and the checkpointed state.
     * The checkpoint is cleared after this method is called.
     */
    public void createCommandHistory() {
    	if (mBuildingCommand != null) {
    		mBuildingCommand.checkpointAfterState();
	    	mCommandHistory.addToCommandHistory(mBuildingCommand);
	    	mBuildingCommand = null;
    	}
    }
    
    /**
     * Undoes the current operation in the token collection's command history.
     */
	public void undo() {
		mCommandHistory.undo();
	}
	
    /**
     * Redoes the current operation in the token collection's command history.
     */
	public void redo() {
		mCommandHistory.redo();
	}
	
	/**
	 * Saves this token collection to the given serialization stream.
	 * @param s The stream to save to.
	 * @throws IOException On write error.
	 */
	public void serialize(MapDataSerializer s) throws IOException {
		s.startArray();
		for (BaseToken t : this.mTokens) {
			t.serialize(s);
		}
		s.endArray();
	}
	
	/**
	 * Populates this token collection from the given deserialization stream.
	 * @param s The stream to load from.
	 * @param tokenDatabase Token database to use when creating tokens.
	 * @throws IOException On read error.
	 */
	public void deserialize(
			MapDataDeserializer s, TokenDatabase tokenDatabase) throws IOException {
		int arrayLevel = s.expectArrayStart();
		while (s.hasMoreArrayItems(arrayLevel)) {
			this.mTokens.add(BaseToken.deserialize(s, tokenDatabase));
		}
		s.expectArrayEnd();
	}
	
	/**
	 * Represents a command that modifies a token.
	 * @author Tim
	 *
	 */
	private class ModifyTokenCommand implements CommandHistory.Command {

		/**
		 * The token that this command modifies.  Should always be the "live"
		 * version of the token.
		 */
		private BaseToken mTokenToModify;
		
		/**
		 * State of the token before modification.
		 */
		private BaseToken mBeforeState;
		
		/**
		 * State of the token after modification.
		 */
		private BaseToken mAfterState;
		
		/**
		 * Constructor.
		 * @param token The token that this command modifies.
		 */
		public ModifyTokenCommand(BaseToken token) {
			mTokenToModify = token;
		}
		
		/**
		 * Saves the initial state of the token being modified.
		 */
		public void checkpointBeforeState() {
			mBeforeState = mTokenToModify.clone();
		}
		
		/**
		 * Saves the final state of the token being modified.
		 */
		public void checkpointAfterState() {
			mAfterState = mTokenToModify.clone();
		}
		
		@Override
		public void execute() {
			mAfterState.copyAttributesTo(mTokenToModify);
			
		}

		@Override
		public void undo() {
			mBeforeState.copyAttributesTo(mTokenToModify);
			
		}

		@Override
		public boolean isNoop() {
			return false;
		}
		
	}
    
	/**
	 * Represents a command that adds or deletes tokens.
	 * @author Tim
	 *
	 */
    private class AddOrDeleteTokenCommand implements CommandHistory.Command {

    	/**
    	 * The token collection being modified.
    	 */
		private TokenCollection mTokenCollection;
		
		/**
		 * List of tokens deleted in this operation.
		 */
    	private List<BaseToken> mTokensToDelete = Lists.newArrayList();
    	
    	/**
    	 * List of tokens added in this operation.
    	 */
    	private List<BaseToken> mTokensToAdd = Lists.newArrayList();
    	

    	/**
    	 * Constructor.
    	 * @param c The token collection to modify.
    	 */
    	public AddOrDeleteTokenCommand(TokenCollection c) {
    		mTokenCollection = c;
    	}


    	/**
    	 * Adds a token to this operation's create list.
    	 * @param t The token being created.
    	 */
    	public void addToken(BaseToken t) {
    		mTokensToAdd.add(t);
    	}
    	
    	/**
    	 * Adds a token to this operation's delete list.
    	 * @param t The token being deleted.
    	 */
    	public void deleteToken(BaseToken t) {
			mTokensToDelete.add(t);
    	}
    	
		@Override
		public void execute() {
			mTokenCollection.mTokens.addAll(mTokensToAdd);
			mTokenCollection.mTokens.removeAll(mTokensToDelete);
		}

		@Override
		public void undo() {
			mTokenCollection.mTokens.removeAll(mTokensToAdd);
			mTokenCollection.mTokens.addAll(mTokensToDelete);
		}

		@Override
		public boolean isNoop() {
			return mTokensToAdd.isEmpty() && mTokensToDelete.isEmpty();
		}
    	

    }

}
