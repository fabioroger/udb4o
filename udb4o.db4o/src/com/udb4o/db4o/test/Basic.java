package com.udb4o.db4o.test;

import org.junit.*;

import com.db4o.*;
import com.udb4o.util.file.*;

public class Basic {
	
	public static class Item {
		
	}
	
	@Test
	public void basic() {
		IFile file = new MemoryFile();
		
		ObjectContainer db = new Udb4oObjectContainer(file);
		
		db.store(new Item());
		db.commit();
		db.close();
		
		db = new Udb4oObjectContainer(file);
		ObjectSet<Item> q = db.query(Item.class);
		Assert.assertEquals(1, q.size());
	}

}
