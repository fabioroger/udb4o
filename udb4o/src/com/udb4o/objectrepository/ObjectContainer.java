package com.udb4o.objectrepository;

public interface ObjectContainer {

	void runTransaction(TransactionalRunnable runner);

}
