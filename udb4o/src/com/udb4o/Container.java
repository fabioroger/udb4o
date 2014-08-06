package com.udb4o;

import java.io.*;

import com.udb4o.storage.util.*;

public interface Container<T> extends Flushable, Closeable {
	
	Slot get(T id);
	
	boolean remove(Slot item);
	
	T getId(Slot item);

	Serializer<T> idSerializer();
	
	void accept(SlotVisitor<T> visitor);

}
