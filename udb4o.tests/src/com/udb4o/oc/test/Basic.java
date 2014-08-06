package com.udb4o.oc.test;

import java.io.*;
import java.util.*;

import junit.framework.Assert;

import org.junit.*;

import com.udb4o.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class Basic {

	IFile file = new RealFile("test.dat");

	@Before
	@After
	public void cleanup() {
		if (file.exists()) {
			file.delete();
		}
	}

	public static class Pilot {
		private String name;
		private int points;

		public Pilot(String name, int points) {
			this.name = name;
			this.points = points;
		}

		public int getPoints() {
			return points;
		}

		public void addPoints(int points) {
			this.points += points;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return name + "/" + points;
		}
	}
	
	@Test
	@Ignore
	public void basic() throws IOException {

		Container<Integer> container = new XContainer<Integer>(file.openAsBuffer(), Serializers.integer);

		ObjectContainer db = new ObjectContainerImpl(container);

		Pilot p1 = new Pilot("Barrichello", 42);

		db.store(p1);

		db.commit();

		db.close();

		Collection<Pilot> pilots = db.query(Pilot.class);

		Assert.assertEquals(1, pilots.size());

		Assert.assertEquals(42, pilots.iterator().next().getPoints());

	}

}
