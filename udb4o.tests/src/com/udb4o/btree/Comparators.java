package com.udb4o.btree;

import java.util.*;

public class Comparators {

	public static Comparator<String> string = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}
		
	};

}
