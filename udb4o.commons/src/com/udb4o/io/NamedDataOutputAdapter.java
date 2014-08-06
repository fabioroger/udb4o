package com.udb4o.io;

import java.io.*;

public class NamedDataOutputAdapter implements NamedDataOutput {
	
	protected DataOutput out;

	public NamedDataOutputAdapter(DataOutput out) {
		this.out = out;
	}

	@Override
	public NamedDataOutput nest(String name) {
		return this;
	}

	@Override
	public void write(String name, int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(String name, byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(String name, byte[] b, int off, int len)
			throws IOException {
		out.write(b, off, len);
	}

	@Override
	public void writeBoolean(String name, boolean v) throws IOException {
		out.writeBoolean(v);
	}

	@Override
	public void writeByte(String name, int v) throws IOException {
		out.writeByte(v);
	}

	@Override
	public void writeShort(String name, int v) throws IOException {
		out.writeShort(v);
	}

	@Override
	public void writeChar(String name, int v) throws IOException {
		out.writeChar(v);
	}

	@Override
	public void writeInt(String name, int v) throws IOException {
		out.writeInt(v);
	}

	@Override
	public void writeLong(String name, long v) throws IOException {
		out.writeLong(v);
	}

	@Override
	public void writeFloat(String name, float v) throws IOException {
		out.writeFloat(v);
	}

	@Override
	public void writeDouble(String name, double v) throws IOException {
		out.writeDouble(v);
	}

	@Override
	public void writeBytes(String name, String s) throws IOException {
		out.writeBytes(s);
	}

	@Override
	public void writeChars(String name, String s) throws IOException {
		out.writeChars(s);
	}

	@Override
	public void writeUTF(String name, String s) throws IOException {
		out.writeUTF(s);
	}
}