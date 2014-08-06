//package com.udb4o.btree;
//
//import org.junit.*;
//
//import com.udb4o.util.file.*;
//import static junit.framework.Assert.*;
//
//public class Sandobox {
//
//
//	@Test
//	public void basic() {
//		IFile root = new MemoryFile();
//		
//		LongBTree btree = new LongBTree(root);
//		
//		Transaction t = btree.beginTransaction();
//		
//		t.put(1, "1".getBytes());
//		
//		t.commit();
//		
//		assertEquals("1".getBytes(), t.get(1));
//		
//		
//	}
//
//}
