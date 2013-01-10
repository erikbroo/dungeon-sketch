package com.tbocek.android.combatmap.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
 * 
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
     * 
     * @param history
     *            Command history to use for undo/redo operations.
     */
    public TokenCollection(CommandHistory history) {
        mCommandHistory = history;
    }

    /**
     * Given a point in screen space, returns the token under that point.
     * 
     * @param p
     *            The point in screen space.
     * @param transformer
     *            Grid space to screen space transformation.
     * @return The token under the point, or null if no tokens.
     */
    public BaseToken getTokenUnderPoint(final PointF p,
            final CoordinateTransformer transformer) {
        for (int i = 0; i < mTokens.size(); ++i) {
            float distance = Util.distance(p, transformer
                    .worldSpaceToScreenSpace(mTokens.get(i).getLocation()));
            if (distance < transformer.worldSpaceToScreenSpace(mTokens.get(i)
                    .getSize() / 2)) {
                return mTokens.get(i);
            }
        }
        return null;
    }

    /**
     * Adds a token to the collection.
     * 
     * @param t
     *            The token to add.
     */
    public void addToken(final BaseToken t) {
        AddTokenCommand c = new AddTokenCommand(this, t);
        mCommandHistory.execute(c);
    }

    /**
     * Removes a token from the collection.
     * 
     * @param t
     *            The token to remove.
     */
    public void remove(final BaseToken t) {
        RemoveTokensCommand c = new RemoveTokensCommand(this, t);
        mCommandHistory.execute(c);
    }

    /**
     * Removes a collection of tokens from the collection.
     * 
     * @param tokens
     *            The tokens to remove.
     */
    public void removeAll(final Collection<BaseToken> tokens) {
        RemoveTokensCommand c = new RemoveTokensCommand(this, tokens);
        mCommandHistory.execute(c);
    }

    /**
     * Finds a location on the given grid to place this token, and places the
     * token there. This allows the token to snap to the grid.
     * 
     * @param t
     *            The token to place.
     * @param attemptedPoint
     *            The location where this token should try to be placed.
     * @param grid
     *            The grid to snap to.
     * @param tokensSnapToIntersections
     *            Whether to place new tokens on line intersections as opposed
     *            to grid spaces.
     */
    public void placeTokenNearby(final BaseToken t,
            final PointF attemptedPoint, final Grid grid,
            boolean tokensSnapToIntersections) {
        int attemptedDistance = 0;
        PointF point = attemptedPoint;
        // Continually increment the attempted distance until an open grid space
        // is found. This is guaranteed to succeed. Note that there are some
        // inefficiencies here (the center point is tried four times, each
        // corner of a square is tried twice, etc). I don't care. This runs
        // fast enough for reasonable token collections on screen.
        point = grid.getNearestSnapPoint(point, tokensSnapToIntersections ? 0
                : t.getSize());
        while (true) {
            // Go clockwise around the size of a square centered on the
            // originally attempted point and with sized of
            // length attemptedDistance*2

            // Across the top
            // The -attemptedDistance + 1 ensures a nice spiral pattern
            for (int i = -attemptedDistance + 1; i <= attemptedDistance; ++i) {
                if (tryToPlaceHere(t, new PointF(point.x + i, point.y
                        - attemptedDistance))) {
                    return;
                }
            }

            // Down the right
            for (int i = -attemptedDistance; i <= attemptedDistance; ++i) {
                if (tryToPlaceHere(t, new PointF(point.x + attemptedDistance,
                        point.y + i))) {
                    return;
                }
            }

            // Across the bottom
            for (int i = attemptedDistance; i >= -attemptedDistance; --i) {
                if (tryToPlaceHere(t, new PointF(point.x + i, point.y
                        + attemptedDistance))) {
                    return;
                }
            }

            // Up the left
            for (int i = attemptedDistance; i >= -attemptedDistance; --i) {
                if (tryToPlaceHere(t, new PointF(point.x - attemptedDistance,
                        point.y + i))) {
                    return;
                }
            }
            attemptedDistance++;
        }
    }

    /**
     * Tries to place the token at the specified location. If a token is already
     * here, returns False. If not, sets the tokens location and returns True.
     * 
     * @param t
     *            The token to try to place.
     * @param point
     *            The location at which to try to place the token.
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
     * 
     * @param point
     *            Center of the token to try placing here.
     * @param radius
     *            Radius of the token to try placing here.
     * @return True if the location is unoccupied, False if there would be an
     *         intersection.
     */
    private boolean isLocationUnoccupied(final PointF point, final double radius) {
        for (BaseToken t : mTokens) {
            if (Util.distance(point, t.getLocation()) < radius + t.getSize()
                    / 2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Draws all tokens.
     * 
     * @param canvas
     *            The canvas to draw on.
     * @param transformer
     *            Transformer from grid space to screen space.
     * @param isDark
     *            Whether to draw as if on a dark background.
     * @param isManipulatable
     *            Whether tokens can currently be manipulated.
     */
    public void drawAllTokens(final Canvas canvas,
            final CoordinateTransformer transformer, boolean isDark,
            boolean isManipulatable) {
        for (int i = 0; i < mTokens.size(); ++i) {
            mTokens.get(i).drawInPosition(canvas, transformer, isDark,
                    isManipulatable);
        }
    }

    /**
     * Computes and returns a bounding rectangle that can contain all the
     * tokens.
     * 
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
     * 
     * @return True if collection is empty.
     */
    public boolean isEmpty() {
        return mTokens.isEmpty();
    }

    /**
     * Sets up this TokenCollection to create a command that modifies the given
     * token. The current state of this token will be duplicated and saved for
     * undo purposes.
     * 
     * @param t
     *            The token to checkpoint.
     */
    public void checkpointToken(BaseToken t) {
        ArrayList<BaseToken> l = Lists.newArrayList();
        l.add(t);
        checkpointTokens(l);
    }

    /**
     * Sets up this TokenCollection to create a command that modifies the given
     * list of tokens. The current state of these tokens will be duplicated and
     * saved for undo purposes.
     * 
     * @param l
     *            List of tokens to checkpoint.
     */
    public void checkpointTokens(List<BaseToken> l) {
        mBuildingCommand = new ModifyTokenCommand(l);
        mBuildingCommand.checkpointBeforeState();
    }

    /**
     * Interrupts the current action that has been checkpointed, and restores
     * the checkpointed token to its initial state.
     */
    public void restoreCheckpointedTokens() {
        if (mBuildingCommand != null) {
            mBuildingCommand.undo();
            mBuildingCommand = null;
        }
    }

    /**
     * Adds an entry to the command history based on the previously checkpointed
     * token. The created command will swap the token's state between a copy of
     * the state when this method was called, and the checkpointed state. The
     * checkpoint is cleared after this method is called.
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

    @Override
    public boolean canUndo() {
        return mCommandHistory.canUndo();
    }

    @Override
    public boolean canRedo() {
        return mCommandHistory.canRedo();
    }

    /**
     * Saves this token collection to the given serialization stream.
     * 
     * @param s
     *            The stream to save to.
     * @throws IOException
     *             On write error.
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
     * 
     * @param s
     *            The stream to load from.
     * @param tokenDatabase
     *            Token database to use when creating tokens.
     * @throws IOException
     *             On read error.
     */
    public void deserialize(MapDataDeserializer s, TokenDatabase tokenDatabase)
            throws IOException {
        int arrayLevel = s.expectArrayStart();
        while (s.hasMoreArrayItems(arrayLevel)) {
            this.mTokens.add(BaseToken.deserialize(s, tokenDatabase));
        }
        s.expectArrayEnd();
    }

    /**
     * Replace all placeholder tokens with actual tokens.
     * 
     * @param tokenDatabase
     *            The database to load new tokens from.
     */
    public void deplaceholderize(TokenDatabase tokenDatabase) {
        for (int i = 0; i < this.mTokens.size(); ++i) {
            BaseToken realToken = mTokens.get(i)
                    .deplaceholderize(tokenDatabase);
            if (realToken != mTokens.get(i)) {
                mTokens.get(i).copyAttributesTo(realToken);
                mTokens.set(i, realToken);
            }
        }
    }

    /**
     * Represents a command that modifies a token.
     * 
     * @author Tim
     * 
     */
    private static class ModifyTokenCommand implements CommandHistory.Command {

        /**
         * The token that this command modifies. Should always be the "live"
         * version of the token.
         */
        private List<BaseToken> mTokensToModify;

        /**
         * State of the token before modification.
         */
        private List<BaseToken> mBeforeState = Lists.newArrayList();

        /**
         * State of the token after modification.
         */
        private List<BaseToken> mAfterState = Lists.newArrayList();

        /**
         * Constructor.
         * 
         * @param tokens
         *            List of tokens that this command modifies.
         */
        public ModifyTokenCommand(List<BaseToken> tokens) {
            mTokensToModify = tokens;
        }

        /**
         * Saves the initial state of the token being modified.
         */
        public void checkpointBeforeState() {
            for (BaseToken t : mTokensToModify) {
                mBeforeState.add(t.clone());
            }
        }

        /**
         * Saves the final state of the token being modified.
         */
        public void checkpointAfterState() {
            for (BaseToken t : mTokensToModify) {
                mAfterState.add(t.clone());
            }
        }

        @Override
        public void execute() {
            for (int i = 0; i < mTokensToModify.size(); ++i) {
                mAfterState.get(i).copyAttributesTo(mTokensToModify.get(i));
            }
        }

        @Override
        public void undo() {
            for (int i = 0; i < mTokensToModify.size(); ++i) {
                mBeforeState.get(i).copyAttributesTo(mTokensToModify.get(i));
            }
        }

        @Override
        public boolean isNoop() {
            return false;
        }

    }

    /**
     * Command that adds a token to the token collection.
     * 
     * @author Tim
     * 
     */
    private static class AddTokenCommand implements CommandHistory.Command {

        /**
         * Token collection to modify.
         */
        private TokenCollection mCollection;

        /**
         * Token to add to the collection.
         */
        private BaseToken mToAdd;

        /**
         * 
         * @param collection
         *            The token collection to modify.
         * @param toAdd
         *            The token to add.
         */
        public AddTokenCommand(TokenCollection collection, BaseToken toAdd) {
            mCollection = collection;
            mToAdd = toAdd;
        }

        @Override
        public void execute() {
            mCollection.mTokens.add(mToAdd);
        }

        @Override
        public void undo() {
            mCollection.mTokens.remove(mToAdd);

        }

        @Override
        public boolean isNoop() {
            return false;
        }
    }

    /**
     * Command that removes a token from the token collection.
     * 
     * @author Tim
     * 
     */
    private static class RemoveTokensCommand implements CommandHistory.Command {

        /**
         * Token collection to modify.
         */
        private TokenCollection mCollection;

        /**
         * Token to remove from the collection.
         */
        private Collection<BaseToken> mToRemove;

        /**
         * Constructor.
         * 
         * @param collection
         *            The token collection to modify.
         * @param toRemove
         *            Collection of tokens to remove.
         */
        public RemoveTokensCommand(TokenCollection collection,
                Collection<BaseToken> toRemove) {
            mCollection = collection;
            mToRemove = toRemove;
        }

        /**
         * Constructor.
         * 
         * @param collection
         *            The token collection to modify.
         * @param toRemove
         *            Single token to remove.
         */
        public RemoveTokensCommand(TokenCollection collection,
                BaseToken toRemove) {
            Collection<BaseToken> arr = Lists.newArrayList();
            arr.add(toRemove);
            mCollection = collection;
            mToRemove = arr;
        }

        @Override
        public void execute() {
            for (BaseToken t : mToRemove) {
                mCollection.mTokens.remove(t);
            }
        }

        @Override
        public void undo() {
            mCollection.mTokens.addAll(mToRemove);
        }

        @Override
        public boolean isNoop() {
            return false;
        }
    }

    public List<BaseToken> asList() {
        return this.mTokens;
    }
}
