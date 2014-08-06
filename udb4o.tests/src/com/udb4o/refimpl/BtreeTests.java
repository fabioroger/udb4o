package com.udb4o.refimpl;

import java.io.*;

import org.junit.*;

import com.udb4o.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class BtreeTests {
	
	public class BTree {

		public BTree(Slot slot) {
			
		}

	}

	@Test
	public void basic() throws IOException {
		
		IFile root = new MemoryFile();
		
		Container<Integer> container = new XContainer<Integer>(root.openAsBuffer(), Serializers.integer);
		Slot slot = container.get(1);

		new BTree(slot);
		
		
		container.flush();
		
	
	}

}
