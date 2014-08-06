package com.udb4o.test;

import java.io.*;
import java.util.*;

import junit.framework.Assert;

import org.junit.*;
import org.junit.Test;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class ContainerTests {
	
	IFile file = new RealFile("file.dat");
	
	@Before
	@After
	public void cleanup() {
		if (file.exists()) {
			file.delete();
		}
	}
	
	@Test
	@Ignore
	public void asBuffer() throws IOException {


		Container<UUID> s = new XContainer<UUID>(file.openAsBuffer(), Serializers.uuid);

		UUID key = UUID.randomUUID();
		RandomAccessBuffer out = s.get(key).asBuffer();
		out.writeUTF("bla");
		out.writeUTF("ble");
		out.flush();
		
		s.flush();

		s.close();

		s = new XContainer<UUID>(file.openAsBuffer(), Serializers.uuid);

		
		out = s.get(key).asBuffer();
		out.position(out.length());
		out.writeUTF("bli");
		out.flush();
		
		s.flush();

		s.close();

		s = new XContainer<UUID>(file.openAsBuffer(), Serializers.uuid);

		RandomAccessBuffer in = s.get(key).asBuffer();
		Assert.assertEquals("bla", in.readUTF());
		Assert.assertEquals("ble", in.readUTF());
		Assert.assertEquals("bli", in.readUTF());
	}

	@Test
	public void asStream() throws IOException {


		Container<UUID> s = new XContainer<UUID>(file.openAsBuffer(), Serializers.uuid);


		UUID key = UUID.randomUUID();
		NamedDataOutput out = s.get(key).asStreamer().openOutput(false);
		out.writeUTF(null, "bla");
		out.writeUTF(null, "ble");
		s.get(key).flush();
		
		s.flush();

		s.close();

		s = new XContainer<UUID>(file.openAsBuffer(), Serializers.uuid);

		
		out = s.get(key).asStreamer().openOutput(true);
		out.writeUTF(null, "bli");
		s.get(key).flush();
		
		s.flush();

		s.close();

		s = new XContainer<UUID>(file.openAsBuffer(), Serializers.uuid);

		NamedDataInput in = s.get(key).asStreamer().openInput();
		Assert.assertEquals("bla", in.readUTF(null));
		Assert.assertEquals("ble", in.readUTF(null));
		Assert.assertEquals("bli", in.readUTF(null));
	}

//	@Test
//	public void writeUpdateBetweenReopenings() {
//
//
//		Storage s = new SimpleFileStorage(file);
//
//		UUID key = UUID.randomUUID();
//		ByteBuffer _42 = ByteBuffer.wrap("bla".getBytes());
//		s.put(key, _42);
//		s.putMore(key, ByteBuffer.wrap("ble".getBytes()));
//
//		s.flush();
//		s.dispose();
//
//		s = new SimpleFileStorage(file);
//		
//		s.putMore(key, ByteBuffer.wrap("bli".getBytes()));
//		
//		s.flush();
//		s.dispose();
//		
//		s = new SimpleFileStorage(file);
//
//		final ByteBuffer full = ByteBuffer.allocate(100);
//
//		final Ref<Integer> count = new Ref<Integer>(0);
//
//		s.get(key, new StorageVisitor() {
//
//			@Override
//			public void visit(ByteBuffer buffer) {
//				full.put(buffer);
//				count.value++;
//			}
//		});
//		
//		full.flip();
//
//		Assert.assertEquals(3, (int) count.value);
//		Assert.assertEquals(ByteBuffer.wrap("blablebli".getBytes()), full);
//	}
//
//	@Test
//	public void consolidate() {
//
//		Storage s = new SimpleFileStorage(file);
//
//		UUID key = UUID.randomUUID();
//		ByteBuffer _42 = ByteBuffer.wrap("bla".getBytes());
//		s.put(key, _42);
//		s.putMore(key, ByteBuffer.wrap("ble".getBytes()));
//
//		s.flush();
//		s.dispose();
//
//		s = new SimpleFileStorage(file);
//		
//		s.put(key, ByteBuffer.wrap("bli".getBytes()));
//		
//		s.flush();
//		s.dispose();
//		
//		s = new SimpleFileStorage(file);
//
//		final ByteBuffer full = ByteBuffer.allocate(100);
//
//		final Ref<Integer> count = new Ref<Integer>(0);
//
//		s.get(key, new StorageVisitor() {
//
//			@Override
//			public void visit(ByteBuffer buffer) {
//				full.put(buffer);
//				count.value++;
//			}
//		});
//		
//		full.flip();
//
//		Assert.assertEquals(1, (int) count.value);
//		Assert.assertEquals(ByteBuffer.wrap("bli".getBytes()), full);
//	}

}
