package com.tbocek.android.combatmap.model;

/**
 * Interface for objects that maintain a command history and a position in that
 * command history, and support undoing and redoing actions.
 * 
 * @author Tim
 * 
 */
public interface UndoRedoTarget {
    /**
     * Undoes the action at the current position.
     */
    void undo();

    /**
     * Redoes the action at the current position.
     */
    void redo();

    /**
     * @return true if there is an action in the queue that can be undone.
     */
    boolean canUndo();

    /**
     * @return true if there is an action in the queue that can be redone.
     */
    boolean canRedo();
}
