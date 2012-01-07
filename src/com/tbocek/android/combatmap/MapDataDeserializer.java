package com.tbocek.android.combatmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;

public class MapDataDeserializer {
	public class SyncException extends IOException {

		public SyncException(String string) {
			super(string);
		}
		
	}
	
	private BufferedReader reader;
	private int arrayLevel = 0;
	private Queue<String> peekBuffer = new LinkedList<String>();
	
	public String readString() throws IOException {
		return nextToken();
	}
	
	public int readInt() throws NumberFormatException, IOException {
		return Integer.parseInt(nextToken());
	}
	
	public float readFloat() throws NumberFormatException, IOException {
		return Float.parseFloat(nextToken());
	}
	
	public boolean readBoolean() throws IOException {
		return nextToken().equals("0") ? false : true;
	}
	
	public void expectArrayStart() {
		// TODO: Implement synchronization logic, at least before map data
		// format needs to be updated.
	}
	
	public void expectArrayEnd() {
		
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
				throw new SyncException("EOF Reached");
			} else if (s == "[") {
				arrayLevel++;
			} else if (s == "]") {
				arrayLevel--;
			}
		} while (s == "[" || s == "]" || s == "");
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
		} while (s == "");
		peekBuffer.add(s);
		return s;
	}
	
	/**
	 * @return The array level at which the next token will be read.
	 * @throws IOException 
	 */
	private int getNextArrayLevel() throws IOException {
		int l = arrayLevel;
		String s;
		do {
			s = peek();
			if (s == "[") {
				l++;
			} else  if (s == "]"){
				l--;
			}
		}while (s == "[" || s == "]");
		return l;
	}

	public boolean hasMoreArrayItems(int terminateAtArrayLevel) throws IOException {
		return terminateAtArrayLevel == getNextArrayLevel();
	}
}
