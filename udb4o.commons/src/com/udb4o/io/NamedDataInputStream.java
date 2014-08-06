package com.udb4o.io;

import java.io.*;

public class NamedDataInputStream implements NamedDataInput, Closeable {

	private DataInputStream in;

	public NamedDataInputStream(DataInputStream in) {
		this.in = in;
	}

	public NamedDataInputStream(InputStream in) {
		this.in = new DataInputStream(in);
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public final void readFully(String name, byte[] b) throws IOException {
		in.readFully(b);
	}

	@Override
	public final void readFully(String name, byte[] b, int off, int len) throws IOException {
		in.readFully(b, off, len);
	}

	@Override
	public final boolean readBoolean(String name) throws IOException {
		return in.readBoolean();
	}

	@Override
	public final byte readByte(String name) throws IOException {
		return in.readByte();
	}

	@Override
	public final int readUnsignedByte(String name) throws IOException {
		return in.readUnsignedByte();
	}

	@Override
	public final short readShort(String name) throws IOException {
		return in.readShort();
	}

	@Override
	public final int readUnsignedShort(String name) throws IOException {
		return in.readUnsignedShort();
	}

	@Override
	public final char readChar(String name) throws IOException {
		return in.readChar();
	}

	@Override
	public final int readInt(String name) throws IOException {
		return in.readInt();
	}

	@Override
	public final long readLong(String name) throws IOException {
		return in.readLong();
	}

	@Override
	public final float readFloat(String name) throws IOException {
		return in.readFloat();
	}

	@Override
	public final double readDouble(String name) throws IOException {
		return in.readDouble();
	}

	@Override
	public final String readUTF(String name) throws IOException {
		return in.readUTF();
	}

	@Override
	public NamedDataInput nest(String name) {
		return this;
	}

	@Override
	public void skip(String name, int bytes) throws IOException {
		in.skipBytes(bytes);
	}

}
