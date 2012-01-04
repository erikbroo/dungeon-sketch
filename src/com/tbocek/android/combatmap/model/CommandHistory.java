package com.tbocek.android.combatmap.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class CommandHistory implements Serializable {
    public interface Command {
    	/**
    	 * Executes the command on the LineCollection that this command mutates.
    	 */
    	void execute();

    	/**
    	 * Undoes the command on the LineCollection that this command mutates.
    	 */
    	public void undo();

    	/**
    	 * @return True if the command is a no-op, false if it modifies lines.
    	 * noop.
    	 */
    	boolean isNoop();
    }
	/**
	 * Operations on this line collection that are available to undo.
	 */
	private transient Stack<Command> toUndo = new Stack<Command>();
	/**
	 * Operations on this line collection that are available to redo.
	 */
	private transient Stack<Command> toRedo = new Stack<Command>();

    /**
     * Undo the last line operation.
     */
    public void undo() {
    	if (canUndo()) {
	    	Command c = toUndo.pop();
	    	c.undo();
	    	toRedo.push(c);
    	}
    }

    /**
     * Redo the last line operation.
     */
    public void redo() {
    	if (canRedo()) {
	    	Command c = toRedo.pop();
	    	c.execute();
	    	toUndo.push(c);
    	}
    }

    /**
     * @return True if the undo operation can be performed, false otherwise.
     */
    public boolean canUndo() {
    	return !toUndo.isEmpty();
    }

    /**
     * @return True if the redo operation can be performed, false otherwise.
     */
    public boolean canRedo() {
    	return !toRedo.isEmpty();
    }

    /**
     * Executes the given command.  This should not be called on commands to
     * redo.
     * @param command The command to execute.
     */
    public void execute(final Command command) {
    	if (!command.isNoop()) {
	    	command.execute();
	    	toUndo.add(command);
	    	toRedo.clear();
    	}
    }
    
    /**
     * Adds the given command to the command history without executing it.
     * @param command The command to add.
     */
    public void addToCommandHistory(final Command command) {
    	toUndo.add(command);
    	toRedo.clear();
    }

    /**
     * Deserializes the object.  This uses the standard deserialization but
     * must also create transient objects that manage undo and redo.
     * @param inputStream Stream to deserialize from.
     * @throws IOException On read error.
     * @throws ClassNotFoundException On deserialization error.
     */
    private void readObject(final ObjectInputStream inputStream)
    		throws IOException, ClassNotFoundException {
    	inputStream.defaultReadObject();
    	toUndo = new Stack<Command>();
    	toRedo = new Stack<Command>();
    }
}
