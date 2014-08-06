package com.udb4o.btree;

public interface Transaction {

	void put(long key, byte[] value);
	
	byte[] get(long key);
	
	void commit();
	
	void rollback();

}