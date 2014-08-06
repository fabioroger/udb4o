package com.udb4o.s0;

import java.io.*;

import org.junit.*;

import com.udb4o.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class Basics {
	
	@Test
	public void start() throws IOException {

		IFile root = new MemoryFile();
		
		Container<Integer> container = new XContainer<Integer>(root.openAsBuffer(), Serializers.integer);
		
		Container<String> p1 = container.get(1).asContainer(Serializers.string);
		p1.get("name").asStreamer().openOutput(false).writeUTF(null, "barrichello");
		p1.get("points").asStreamer().openOutput(false).writeInt(null, 42);
		p1.flush();
		
		container.flush();
		container.close();
		
		
		container = new XContainer<Integer>(root.openAsBuffer(), Serializers.integer);
		
		p1 = container.get(1).asContainer(Serializers.string);
		Assert.assertEquals("barrichello", p1.get("name").asStreamer().openInput().readUTF(null));
		Assert.assertEquals(42, p1.get("points").asStreamer().openInput().readInt(null));
		
		container.close();

	
		
	}

}
