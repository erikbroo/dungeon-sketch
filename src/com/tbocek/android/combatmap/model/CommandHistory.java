package com.tbocek.android.combatmap.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Stack;

/**
 * Implements a queue of commands, and an interface to move through the queue
 * using undo and redo operations.
 * 
 * @author Tim
 * 
 */
public class CommandHistory {
	/**
	 * Interface defining the operations that commands should support.
	 * 
	 * @author Tim
	 * 
	 */
	public interface Command {
		/**
		 * Executes the command on the LineCollection that this command mutates.
		 */
		void execute();

		/**
		 * Undoes the command on the LineCollection that this command mutates.
		 */
		void undo();

		/**
		 * @return True if the command is a no-op, false if it modifies lines.
		 *         noop.
		 */
		boolean isNoop();
	}

	/**
	 * Operations on this line collection that are available to undo.
	 */
	private transient Stack<Command> mUndo = new Stack<Command>();
	/**
	 * Operations on this line collection that are available to redo.
	 */
	private transient Stack<Command> mRedo = new Stack<Command>();

	/**
	 * Undo the last line operation.
	 */
	public void undo() {
		if (canUndo()) {
			Command c = mUndo.pop();
			c.undo();
			mRedo.push(c);
		}
	}

	/**
	 * Redo the last line operation.
	 */
	public void redo() {
		if (canRedo()) {
			Command c = mRedo.pop();
			c.execute();
			mUndo.push(c);
		}
	}

	/**
	 * @return True if the undo operation can be performed, false otherwise.
	 */
	public boolean canUndo() {
		return !mUndo.isEmpty();
	}

	/**
	 * @return True if the redo operation can be performed, false otherwise.
	 */
	public boolean canRedo() {
		return !mRedo.isEmpty();
	}

	/**
	 * Executes the given command. This should not be called on commands to
	 * redo.
	 * 
	 * @param command
	 *            The command to execute.
	 */
	public void execute(final Command command) {
		if (!command.isNoop()) {
			command.execute();
			mUndo.add(command);
			mRedo.clear();
		}
	}

	/**
	 * Adds the given command to the command history without executing it.
	 * 
	 * @param command
	 *            The command to add.
	 */
	public void addToCommandHistory(final Command command) {
		mUndo.add(command);
		mRedo.clear();
	}

	/**
	 * Deserializes the object. This uses the standard deserialization but must
	 * also create transient objects that manage undo and redo.
	 * 
	 * @param inputStream
	 *            Stream to deserialize from.
	 * @throws IOException
	 *             On read error.
	 * @throws ClassNotFoundException
	 *             On deserialization error.
	 */
	private void readObject(final ObjectInputStream inputStream)
			throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		mUndo = new Stack<Command>();
		mRedo = new Stack<Command>();
	}
}
