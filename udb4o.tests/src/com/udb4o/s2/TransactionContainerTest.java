package com.udb4o.s2;

import java.io.*;

import org.junit.*;

import com.udb4o.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class TransactionContainerTest {
	
	IFile file = new RealFile("test.dat");

	@Before
	@After
	public void cleanup() {
		if (file.exists()) {
			file.delete();
		}
	}
	
	@Test
	@Ignore
	public void basic() throws IOException {

		Container<Integer> container = new XContainer<Integer>(file.openAsBuffer(), Serializers.integer);
		TransactionManager<Integer> tc = new SimpleTransactionManager<Integer>(container);

		TransactionalContainer<Integer> t1 = tc.beginTransaction();

		t1.get(0).asBuffer().writeUTF("zero");
		t1.get(1).asBuffer().writeUTF("one");
		
		TransactionalContainer<Integer> t2 = tc.beginTransaction();
		Assert.assertTrue(t2.get(0).isEmpty());
		
		t1.commit();

		t1 = tc.beginTransaction();
		
		Assert.assertEquals("one", t1.get(1).asBuffer().readUTF());

		t1.get(0).asBuffer().writeUTF("zero");
		t1.get(1).asBuffer().writeUTF("one");
		
		t1.commit();
	}


}
