package com.udb4o.btree;

import com.udb4o.refimpl.*;

public class Hashers {
	
	public static Hasher<Integer, String> stringToInt = new Hasher<Integer, String>() {
		@Override
		public Integer hash(String id) {
			return id.hashCode();
		}

		@Override
		public void update(String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Integer digest() {
			throw new UnsupportedOperationException();
		}
	};

}
