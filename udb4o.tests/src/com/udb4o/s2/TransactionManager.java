package com.udb4o.s2;

public interface TransactionManager<T> {
	
	TransactionalContainer<T> beginTransaction();

}
