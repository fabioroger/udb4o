package com.udb4o.io;

import java.io.*;

public interface NamedDataOutput {
	
	NamedDataOutput nest(String name);
	
	void write(String name, int b) throws IOException;

	void write(String name, byte b[]) throws IOException;

	void write(String name, byte b[], int off, int len) throws IOException;

	void writeBoolean(String name, boolean v) throws IOException;

	void writeByte(String name, int v) throws IOException;

	void writeShort(String name, int v) throws IOException;

	void writeChar(String name, int v) throws IOException;

	void writeInt(String name, int v) throws IOException;

	void writeLong(String name, long v) throws IOException;

	void writeFloat(String name, float v) throws IOException;

	void writeDouble(String name, double v) throws IOException;

	void writeBytes(String name, String s) throws IOException;

	void writeChars(String name, String s) throws IOException;

	void writeUTF(String name, String s) throws IOException;
}
