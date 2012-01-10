package com.tbocek.android.combatmap.model.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

public class MapDataDeserializer {
	public class SyncException extends IOException {
		public SyncException(String string) {
			super(string);
		}
	}
	
	public MapDataDeserializer(BufferedReader reader) {
		this.reader = reader;
	}
	
	private BufferedReader reader;
	private int arrayLevel;
	private LinkedList<String> peekBuffer = new LinkedList<String>();
	
	public String readString() throws IOException {
		return nextToken();
	}
	
	public int readInt() throws IOException {
		return Integer.parseInt(nextToken());
	}
	
	public float readFloat() throws IOException {
		return Float.parseFloat(nextToken());
	}
	
	public boolean readBoolean() throws IOException {
		return nextToken().equals("0") ? false : true;
	}
	
	public int expectArrayStart() throws IOException {
		String t = nextToken();
		if (t.equals("[")) {
			arrayLevel++;
		} else {
			throw new SyncException("Expected array start, got " + t);
		}
		// Return the array level at which this array will end.
		return arrayLevel - 1;
	}
	
	public void expectArrayEnd() throws IOException {
		String t = nextToken();
		if (t.equals("]")) {
			arrayLevel--;
		} else {
			throw new SyncException("Expected array end, got " + t);
		}
	}
	
	public void expectObjectStart() throws IOException {
		String t = nextToken();
		if (!t.equals("{")) {
			throw new SyncException("Expected object start, got " + t);
		}
	}
	
	public void expectObjectEnd() throws IOException {
		String t = nextToken();
		if (!t.equals("}")) {
			throw new SyncException("Expected object end, got " + t);
		}
	}
	
	private String nextToken() throws IOException {
		String s;
		do {
			if (peekBuffer.size() == 0) {
				s = reader.readLine();
			} else {
				s = peekBuffer.remove();
			}
			
			if (s == null) {
				return null;
			}
		} while (s.equals(""));
		//Log.d("com.tbocek.android.combatmap.model.io.MapDataDeserializer", s);
		return s;
	}
	
	public int getArrayLevel() {
		// If the next call to read would end the array, we want to reflect that
		// in the array level
		return arrayLevel;
	}
	
	private String peek() throws IOException {
		String s;
		do {
			s = reader.readLine();
		} while (s != null && s.equals(""));
		if (s != null)
			peekBuffer.add(s);
		return s;
	}
	
	/**
	 * @param terminateAtArrayLevel Array level at which to terminate the search.
	 * @return The array level at which the next token will be read.
	 * @throws IOException 
	 */
	private int getNextArrayLevel() throws IOException {
		int l = arrayLevel;
		String s;
		do {
			s = peek();
			if (s == null) {
				break;
			}
			
			if (s.equals("]")){
				l--;
			}
		}while (s.equals("]"));
		return l;
	}

	public boolean hasMoreArrayItems(int terminateAtArrayLevel) throws IOException {
		return terminateAtArrayLevel < getNextArrayLevel();
	}
}
