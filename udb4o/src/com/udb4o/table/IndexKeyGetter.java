package com.udb4o.table;

public interface IndexKeyGetter<T, FT> {
	
	FT key(T record);
	
}