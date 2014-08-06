package com.udb4o.io;

import java.io.*;

public class NamedDataOutputStream extends NamedDataOutputAdapter implements NamedDataOutput, Flushable, Closeable {

	public NamedDataOutputStream(DataOutputStream out) {
		super(out);
	}

	private DataOutputStream out() {
		return (DataOutputStream) out;
	}
	
	public NamedDataOutputStream(OutputStream out) {
		super(new DataOutputStream(out));
	}

	@Override
	public void flush() throws IOException {
		out().flush();
	}

	@Override
	public void close() throws IOException {
		out().close();
	}

	public final int size() {
		return out().size();
	}
	
}
