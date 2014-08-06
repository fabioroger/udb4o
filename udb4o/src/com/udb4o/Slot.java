package com.udb4o;

import java.io.*;

import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public interface Slot extends Flushable, Closeable, Hanger {
	
	void remove() throws IOException;
	
	boolean isEmpty();
	
	Streamer asStreamer();
	RandomAccessBuffer asBuffer();
	
	<T> Container<T> asContainer(Serializer<T> idSerializer);
	
	StorageStrategyHints storageHints();
	
}
