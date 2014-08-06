package com.udb4o.table;

import java.util.*;

public interface Index<T, FT> {
	
	List<T> getAll(FT key);
	
	T first(FT key);
	
	void drop();

}
