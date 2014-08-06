package com.udb4o.s2;

import java.io.*;

import com.udb4o.*;
import com.udb4o.storage.util.*;

public class SimpleTransactionManager<T> implements TransactionManager<T> {

	private final class TransactionalContainerImpl implements TransactionalContainer<T> {
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean remove(Slot item) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Serializer<T> idSerializer() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T getId(Slot item) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Slot get(T id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void commit() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void accept(SlotVisitor<T> visitor) {
			// TODO Auto-generated method stub
			
		}
	}

//	private final Container<T> container;

	public SimpleTransactionManager(Container<T> container) {
//		this.container = container;
	}

	@Override
	public TransactionalContainer<T> beginTransaction() {
		return new TransactionalContainerImpl();
	}

}
