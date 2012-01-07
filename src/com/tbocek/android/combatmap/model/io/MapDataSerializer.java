package com.tbocek.android.combatmap.model.io;

import java.io.IOException;
import java.io.Writer;

/**
 * A fast serializer/deserializer that is also built to robustly allow object changes.
 * Format is 
 * object 
 * = 
 * 1 
 * 2 
 * 3 
 * a 
 * b 
 * c 
 * [ 
 * a 
 * r 
 * r 
 * a 
 * y 
 * ] 
 * multi word string 
 * subobject 
 * = 
 * 4 
 * f 
 * [ 
 * 6.1 
 * 6.2 
 * ] 
 * ; 
 * ; 
 * Line breaks are mandatory
 * @author Tim
 *
 */
public class MapDataSerializer {
	private Writer writer;
	
	public MapDataSerializer(Writer writer) {
		this.writer = writer;
	}
	
	public void serializeString(String value) throws IOException {
		boolean needsQuote = false;
		writer.write(value);
		writer.write('\n');
	}
	
	public void serializeInt(int value) throws IOException {
		writer.write(Integer.toString(value));
		writer.write('\n');
	}
	
	public void serializeFloat(float value) throws IOException {
		writer.write(Float.toString(value));
		writer.write('\n');
	}
	
	public void serializeBoolean(boolean value) throws IOException {
		writer.write(value ? "1\n" : "0\n");
	}
	
	public void startArray() throws IOException {
		writer.write("[\n");
	}
	
	public void endArray() throws IOException {
		writer.write("]\n");
	}
}
