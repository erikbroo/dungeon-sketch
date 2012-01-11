package com.tbocek.android.combatmap.model.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Wraps a BufferedReader to provide a layer of functions specific to the
 * map data format.
 * @author Tim
 *
 */
public class MapDataDeserializer {
	/**
	 * Thrown when the start/end of an object/array is not at the expected
	 * place.
	 * @author Tim
	 *
	 */
	public class SyncException extends IOException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9205252802710503280L;

		/**
		 * Constructor.
		 * @param string Exception text.
		 */
		public SyncException(String string) {
			super(string);
		}
	}
	
	/**
	 * The reader to read from.
	 */
	private BufferedReader mReader;
	
	/**
	 * The current number of nested arrays.
	 */
	private int mArrayLevel;
	
	/**
	 * A buffer for tokens that we have peeked at.  This allows us to look
	 * ahead without consuming.
	 */
	private LinkedList<String> mPeekBuffer = new LinkedList<String>();
	
	/**
	 * Constructor.
	 * @param reader The reader to read from.
	 */
	public MapDataDeserializer(BufferedReader reader) {
		this.mReader = reader;
	}

	/**
	 * Consumes and returns a string value.
	 * @return The read value.
	 * @throws IOException On read error.
	 */
	public String readString() throws IOException {
		return nextToken();
	}

	/**
	 * Consumes and returns an integer value.
	 * @return The read value.
	 * @throws IOException On read error.
	 */
	public int readInt() throws IOException {
		return Integer.parseInt(nextToken());
	}
	
	/**
	 * Consumes and returns a floating point value.
	 * @return The read value.
	 * @throws IOException On read error.
	 */
	public float readFloat() throws IOException {
		return Float.parseFloat(nextToken());
	}
	
	/**
	 * Consumes and returns a boolean value.
	 * @return The read value.
	 * @throws IOException On read error.
	 */
	public boolean readBoolean() throws IOException {
		return !nextToken().equals("0");
	}

	/**
	 * Consumes the start of an array if we are at the end of an object.  If
	 * not, throws an exception.
	 * @return The array level that we *left* by starting the array.
	 * @throws IOException If we are not at the start of the array as expected.
	 */
	public int expectArrayStart() throws IOException {
		String t = nextToken();
		if (t.equals("[")) {
			mArrayLevel++;
		} else {
			throw new SyncException("Expected array start, got " + t);
		}
		// Return the array level at which this array will end.
		return mArrayLevel - 1;
	}
	

	/**
	 * Consumes the end of an array if we are at the end of an object.  If not,
	 * throws an exception.
	 * @throws IOException If we are not at the end of the array as expected.
	 */
	public void expectArrayEnd() throws IOException {
		String t = nextToken();
		if (t.equals("]")) {
			mArrayLevel--;
		} else {
			throw new SyncException("Expected array end, got " + t);
		}
	}

	/**
	 * Consumes the start of an object if we are at the end of an object.  If
	 * not, throws an exception.
	 * @throws IOException If we are not at the start of the object as expected.
	 */
	public void expectObjectStart() throws IOException {
		String t = nextToken();
		if (!t.equals("{")) {
			throw new SyncException("Expected object start, got " + t);
		}
	}
	
	/**
	 * Consumes the end of an object if we are at the end of an object.  If not,
	 * throws an exception.
	 * @throws IOException If we are not at the end of the object as expected.
	 */
	public void expectObjectEnd() throws IOException {
		String t = nextToken();
		if (!t.equals("}")) {
			throw new SyncException("Expected object end, got " + t);
		}
	}
	
	/**
	 * Reads and returns a token.
	 * @return The read token, or null if at EOF.
	 * @throws IOException On read error.
	 */
	private String nextToken() throws IOException {
		String s;
		do {
			if (mPeekBuffer.size() == 0) {
				s = mReader.readLine();
			} else {
				s = mPeekBuffer.remove();
			}
			
			if (s == null) {
				return null;
			}
		} while (s.equals(""));
		return s;
	}
	
	/**
	 * @return The current amount of array nesting.
	 */
	public int getArrayLevel() {
		return mArrayLevel;
	}
	
	/**
	 * Reads and returns a token from the stream, placing it in a queue of
	 * tokens that have already been peeked at.
	 * @return The read token.
	 * @throws IOException On read error.
	 */
	private String peek() throws IOException {
		String s;
		do {
			s = mReader.readLine();
		} while (s != null && s.equals(""));
		if (s != null) {
			mPeekBuffer.add(s);
		}
		return s;
	}
	
	/**
	 * @return The array level at which the next token will be read.
	 * @throws IOException 
	 */
	private int getNextArrayLevel() throws IOException {
		int l = mArrayLevel;
		String s;
		do {
			s = peek();
			if (s == null) {
				break;
			}
			
			if (s.equals("]")) {
				l--;
			}
		} while (s.equals("]"));
		return l;
	}

	/**
	 * Checks whether this array has more items.
	 * @param terminateAtArrayLevel One less than this array's level (should be
	 * 		the return value from expectArrayStart().
	 * @return True if there is another item, False otherwise.
	 * @throws IOException On read error (since we need to prefetch some tokens)
	 */
	public boolean hasMoreArrayItems(int terminateAtArrayLevel) 
			throws IOException {
		return terminateAtArrayLevel < getNextArrayLevel();
	}
}
