package com.udb4o.gc;

import com.udb4o.*;

public interface GCContainer<T> extends Container<T> {
	
	void gc(long now);
	
	void addRoot(Slot storable, long expirationDate);
	
	void remoteRoot(Slot storable);

}
