package com.udb4o.badu;

import java.io.*;
import java.security.*;
import java.util.*;

import org.junit.*;
import org.xtreemfs.babudb.*;
import org.xtreemfs.babudb.api.*;
import org.xtreemfs.babudb.api.database.*;
import org.xtreemfs.babudb.api.exception.*;
import org.xtreemfs.babudb.config.*;
import org.xtreemfs.babudb.log.DiskLogger.SyncMode;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;

public class BaduBasics {
	
	private DatabaseManager dbm;
	private Database db;
	private BabuDB databaseSystem;

	@Before
	public void setup() throws BabuDBException {
		databaseSystem = BabuDBFactory.createBabuDB(new BabuDBConfig("babudb/databases/", "babudb/dblog/", 4, 1024 * 1024 * 16, 5 * 60, SyncMode.FSYNC,
			50, 0, false, 16, 1024 * 1024 * 512));
		dbm = databaseSystem.getDatabaseManager();
		db = dbm.getDatabases().get("test");
		if (db != null) {
			dbm.deleteDatabase("test");
		}
		db = dbm.createDatabase("test", 1);
	}
	
	@After
	public void teardown() throws BabuDBException {
		
		db.shutdown();
//		dbm.deleteDatabase("test");
		databaseSystem.shutdown();
	}
	
	@Test
	public void simple() throws IOException, BabuDBException {

		
		populate(db, 1);
		populate(db, 2);
		read(db, 1);
		read(db, 2);
		read(db, 1);

	}

	private void populate(Database db, int id) throws IOException, BabuDBException {
		
		Container<Integer> container = new BaduContainer<Integer>(db, Serializers.integer);

		Container<String> p1 = container.get(id).asContainer(Serializers.string);
		p1.get("name").asStreamer().openOutput(false).writeUTF("value", "barrichello");
		p1.get("points").asStreamer().openOutput(false).writeInt("value", 42);

		container.flush();
		container.close();
	}

	private void read(Database db, int id) throws IOException, BabuDBException {
		
		Container<Integer> container = new BaduContainer<Integer>(db, Serializers.integer);
		
		Container<String> p1 = container.get(id).asContainer(Serializers.string);
		Assert.assertEquals("barrichello", p1.get("name").asStreamer().openInput().readUTF("value"));
		Assert.assertEquals(42, p1.get("points").asStreamer().openInput().readInt("value"));
		
		container.close();
	}
	@Test
	public void append() throws IOException, BabuDBException {

		Container<Integer> container;

		container = new BaduContainer<Integer>(db, Serializers.integer);

		container.get(1).asStreamer().openOutput(false).writeUTF(null, "1");

		container.flush();

		container.get(1).asStreamer().openOutput(false).writeUTF(null, "2");

		container.flush();

		
		NamedDataInput in = container.get(1).asStreamer().openInput();
		Assert.assertEquals("2", in.readUTF(null));
		try {
			String ret = in.readUTF(null);
			Assert.fail("last call should have thrown, but it returned: "+ret);
		} catch (EOFException expected) {
		}



		container.get(1).asStreamer().openOutput(false).writeUTF(null, "1");
		container.flush();
		container.get(1).asStreamer().openOutput(true).writeUTF(null, "2");

		container.flush();
		container.close();
		
		
		container = new BaduContainer<Integer>(db, Serializers.integer);

		in = container.get(1).asStreamer().openInput();
		Assert.assertEquals("1", in.readUTF(null));
		Assert.assertEquals("2", in.readUTF(null));
		try {
			String ret = in.readUTF(null);
			Assert.fail("last call should have thrown, but it returned: "+ret);
		} catch (EOFException exptected) {
		}

		container.close();
	}
	
	
//	@Test
//	public void testHasher() {
//		Sha1Hasher<Integer> hasher = new Sha1Hasher<Integer>(Serializers.integer);
//		
//		Assert.assertEquals(hasher.hash(2), hasher.hash(2));
//	}
//	
//	@Test
//	public void expectedSize() throws IOException {
//		
//		IFile root = new MemoryFile();
//		
//		Container<Integer> container;
//		Slot slot;
//		
//		container = new XContainer<Integer>(root.openAsBuffer(), Serializers.integer);
//		slot = container.get(1);
//		slot.storageHints().expectedSize(128);
//		slot.asStreamer().openOutput(true).writeInt(null, 1);
//		slot.asStreamer().openOutput(true).writeInt(null, 1);
//		container.flush();
//		slot.asStreamer().openOutput(true).writeInt(null, 1);
//		container.get(2).storageHints().expectedSize(128);
//		container.get(2).asStreamer().openOutput(true).writeUTF(null, "deadbeef");
//		container.flush();
//		container.close();
//		
//		long size = root.openAsBuffer().length();
//
//		container = new XContainer<Integer>(root.openAsBuffer(), Serializers.integer);
//		
//		Assert.assertEquals("deadbeef", container.get(2).asStreamer().openInput().readUTF(null));
//		
//		slot = container.get(1);
//		slot.storageHints().expectedSize(128);
//		slot.asStreamer().openOutput(true).writeInt(null, 1);
//		container.flush();
//		container.close();
//		
//		Assert.assertEquals(size, root.openAsBuffer().length());
//
//	}
//	
	@Test
	public void simpleHierarchical() throws IOException, BabuDBException {
		
		Container<Integer> container;
		
		container = new BaduContainer<Integer>(db, Serializers.integer);
		
		container.get(1).asContainer(Serializers.integer).get(1).asStreamer().openOutput(true).writeInt(null, 1);
		container.flush();
		container.get(1).asContainer(Serializers.integer).get(1).asStreamer().openOutput(true).writeInt(null, 1);
		
		container.flush();
		container.close();

		container = new BaduContainer<Integer>(db, Serializers.integer);
		NamedDataInput in = container.get(1).asContainer(Serializers.integer).get(1).asStreamer().openInput();
		Assert.assertEquals(1, in.readInt(null));
		Assert.assertEquals(1, in.readInt(null));
		
		try {
			in.readInt(null);
			Assert.fail();
		} catch (EOFException expected) {
			// nothing
		}

		
	}
	
	@Test
	public void scale() throws IOException, NoSuchAlgorithmException, BabuDBException {
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream tasks = new DataOutputStream(bout);
		
		Container<Integer> container = new BaduContainer<Integer>(db, Serializers.integer);
		
		final Random random = new Random(1);
		
		int count = 0;
		int flushes = 0;
		Set<Integer> objs = new HashSet<Integer>();
		
		while(count < 1000) {
			
			count++;
			
			int slotId = random.nextInt(10000);
			objs.add(slotId);
			
			tasks.writeInt(slotId);
			
			Slot slot = container.get(slotId);
			if (slot.isEmpty() && random.nextInt(30) == 0) {
				slot.storageHints().expectedSize(128);
			}
			NamedDataOutput out = slot.asStreamer().openOutput(true);
			
			while(random.nextInt(10) == 0) {
				int op = random.nextInt(6);
				tasks.writeByte(op);
	
				switch(op) {
				case 0:
					int nextInt = random.nextInt(100);
					out.writeInt(null, nextInt);
					tasks.writeInt(nextInt);
					break;
				case 1:
					out.writeUTF(null, "deadbeef");
					break;
				case 2:
					double nextDouble = random.nextDouble();
					out.writeDouble(null, nextDouble);
					tasks.writeDouble(nextDouble);
					break;
				case 3:
				case 4:
				case 5:
					int child = random.nextInt(10);
					tasks.writeByte(child);
					out = slot.asContainer(Serializers.integer).get(child).asStreamer().openOutput(true);
					break;
				}
			}
			tasks.writeByte(-1);
			slot.flush();
			if (random.nextInt(1000) == 0) {
				flushes++;
				container.flush();
			}
					
					
		}

		System.out.println("operations: "+ count);
		System.out.println("objects: "+ objs.size());
		System.out.println("syncs: "+ flushes);
		
		tasks.writeInt(-1);
		tasks.flush();
		
		container.flush();
		container.close();
		
		container = new BaduContainer<Integer>(db, Serializers.integer);
		
		
		DataInputStream tasksIn = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
		Map<UUID, NamedDataInput> ins = new HashMap<UUID, NamedDataInput>();
		int id;
		int actualCount = 0;
		while((id=tasksIn.readInt())!=-1) {
			actualCount++;
			NamedDataInput in = ins.get(hash(id));
			Slot slot = container.get(id);
			if (in == null) {
				in = slot.asStreamer().openInput();
				ins.put(hash(id), in);
			}
			int op;
			while((op=tasksIn.readByte())!=-1) {
				switch(op) {
				case 0:
					Assert.assertEquals(tasksIn.readInt(), in.readInt(null));
					break;
				case 1:
					Assert.assertEquals("deadbeef", in.readUTF(null));
					break;
				case 2:
					Assert.assertEquals(tasksIn.readDouble(), in.readDouble(null), .001);
					break;
				case 3:
				case 4:
				case 5:
					int child = (int)tasksIn.readByte();
					in = ins.get(hash(id, child));
					if (in == null) {
						in = slot.asContainer(Serializers.integer).get(child).asStreamer().openInput();
						ins.put(hash(id, child), in);
					}
					break;
				}
			}
		}
		
		Assert.assertEquals(count, actualCount);
		
		container.close();
		
	}
	
	static private Sha1Hasher<Integer> hasher = new Sha1Hasher<Integer>(Serializers.integer);
	static public UUID hash(int... ids) {
		for(int id : ids) {
			hasher.update(id);
		}
		return hasher.digest();
	}

}
