package com.udb4o.s2;

import com.udb4o.*;

public interface TransactionalContainer<T> extends Container<T>{
	
	void commit();

}
