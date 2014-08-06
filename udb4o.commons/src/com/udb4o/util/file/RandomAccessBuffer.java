package com.udb4o.util.file;

import java.io.*;

public interface RandomAccessBuffer extends DataOutput, DataInput, Flushable, Closeable {

	long position() throws IOException;
	void position(long newPosition) throws IOException;

	long length() throws IOException;
	void setLength(long newLength) throws IOException;

	boolean isEmpty();

}
