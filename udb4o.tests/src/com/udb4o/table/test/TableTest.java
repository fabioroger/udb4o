package com.udb4o.table.test;

import static org.junit.Assert.*;

import org.junit.*;

import com.udb4o.table.*;
import com.udb4o.table.impl.*;

public class TableTest {

	public static class Pilot {
		
		private String name;
		private int points;

		public Pilot(String name, int points) {
			super();
			this.name = name;
			this.points = points;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPoints() {
			return points;
		}

		public void setPoints(int points) {
			this.points = points;
		}

	}
	
	@Test
	public void basic() {
		
		IndexableList<Pilot> pilots = new HeapIndexableList<Pilot>();
		
		Index<Pilot, Integer> index = pilots.createIndex(new IndexKeyGetter<Pilot, Integer>(){

			@Override
			public Integer key(Pilot record) {
				return record.getPoints();
			}
			
		});
		
		pilots.add(new Pilot("Barichello", 42));
		
		Index<Pilot, String> i = pilots.createIndex(new IndexKeyGetter<TableTest.Pilot, String>() {

			@Override
			public String key(Pilot record) {
				return record.getName();
			}
		});
		
		
		assertEquals("Barichello", index.first(42).getName());
		assertEquals(42, i.first("Barichello").getPoints());
		

	}

}
