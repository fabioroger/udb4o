package com.udb4o.io;

import java.io.*;

public interface NamedDataInput {
	
	NamedDataInput nest(String name);

	void readFully(String name, byte b[]) throws IOException;

	void readFully(String name, byte b[], int off, int len) throws IOException;

	boolean readBoolean(String name) throws IOException;

	byte readByte(String name) throws IOException;

	int readUnsignedByte(String name) throws IOException;

	short readShort(String name) throws IOException;

	int readUnsignedShort(String name) throws IOException;

	char readChar(String name) throws IOException;

	int readInt(String name) throws IOException;

	long readLong(String name) throws IOException;

	float readFloat(String name) throws IOException;

	double readDouble(String name) throws IOException;

	String readUTF(String name) throws IOException;
	
	void skip(String name, int bytes) throws IOException;
}
