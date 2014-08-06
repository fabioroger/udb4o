//package com.udb4o.test;
//
//import java.io.*;
//import java.util.*;
//
//import junit.framework.Assert;
//
//import org.junit.*;
//import org.junit.Test;
//
//public class DataContainerTests {
//	
//	File file = new File("file.dat");
//	
//	@After
//	public void tearDown() {
//		if (file.exists()) {
//			file.delete();
//		}
//	}
//
//	private DataContainer openDataStorage() {
//		return new SimpleDataContainer(new SimpleFileStorage(file));
//	}
//
//	@Test
//	public void basic() {
//
//		DataContainer ds = openDataStorage();
//		
//		UUID key = UUID.randomUUID();
//		
//		Data data = ds.createData(key);
//		
//		data.set(0, "bla");
//		
//		ds.flush();
//		
//		ds.dispose();
//		
//		ds = openDataStorage();
//		
//		Assert.assertEquals("bla", ds.getData(key).get(0));
//		
//		ds.dispose();
//
//	}
//
//
//
//
//}
