package com.udb4o.refimpl;


public interface Hasher<H, V> {

	H hash(V id);

	void update(V value);
	H digest();
	
}
