package com.udb4o.badu;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.xtreemfs.babudb.*;
import org.xtreemfs.babudb.api.*;
import org.xtreemfs.babudb.api.database.*;
import org.xtreemfs.babudb.api.exception.*;
import org.xtreemfs.babudb.config.*;
import org.xtreemfs.babudb.log.DiskLogger.SyncMode;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class BaduContainer<T> implements Container<T>, BaduFlushable, BaduFlusher {

	private static final NamedDataInputStream EMPTY_NAMED_DATA_INPUT_STREAM = new NamedDataInputStream(new ByteArrayInputStream(new byte[0]));
	private static final BaduFlusher EMPTY_FLUSHER = new BaduFlusher() {
		
		@Override
		public void markDirty(BaduFlushable flushable) {
		}
		
		@Override
		public void markClean(BaduFlushable flushable) {
		}
	};
	
	private final class BaduSlot implements Slot, BaduFlushable, BaduFlusher {
		private final T id;
		private Object opaque;
		private RandomAccessByteArray buffer;
		private byte[] serializedId;
		private Serializer<T> idSerializerParent;
		private Set<BaduFlushable> dirty = new HashSet<BaduFlushable>();
		private byte[] result;

		private BaduSlot(T id, byte[] serializedId, Serializer<T> idSerializerParent) {
			this.id = id;
			this.serializedId = serializedId;
			this.idSerializerParent = idSerializerParent;
		}

		public BaduSlot(T id, byte[] key, Serializer<T> idSerializer, byte[] buffer) {
			this.id = id;
			// TODO Auto-generated constructor stub
			serializedId = key;
			idSerializerParent = idSerializer;
			this.buffer = new RandomAccessByteArray(buffer);
		}

		@Override
		public void setOpaque(Object opaque) {
			this.opaque = opaque;
		}

		@Override
		public Object getOpaque() {
			return opaque;
		}

		@Override
		public void close() throws IOException {
		}
		
		@Override
		public boolean flushTo(DatabaseInsertGroup ig) throws IOException {
			if (dirty.isEmpty() && (buffer == null || buffer.isEmpty() || !buffer.isDirty())) {
				return false;
			}
			boolean ret = false;
			if (!dirty.isEmpty()) {
				for(BaduFlushable flushable : dirty) {
					ret |= flushable.flushTo(ig);
				}
				dirty.clear();
			}

			if (buffer != null && !buffer.isEmpty() && buffer.isDirty()) {
				ret = true;
				ig.addInsert(0, serializedId, toByteArray());
			}
			return ret;
		}

		@Override
		public void flush() throws IOException {
			DatabaseInsertGroup ig = db.createInsertGroup();
			if (flushTo(ig)) {
				try {
					db.insert(ig, null).get();
				} catch (BabuDBException e) {
					throw new RuntimeException(e);
				}
			}
			parentFlusher.markClean(this);
		}
		
		@Override
		public void markClean(BaduFlushable flushable) {
			dirty.remove(flushable);
			if (dirty.isEmpty()) {
				BaduContainer.this.markClean(this);
			}
		}
		
		@Override
		public void markDirty(BaduFlushable flushable) {
			if (flushable == null) {
				System.out.println("---> here");
			}
			dirty.add(flushable);
			if (dirty.size() == 1) {
				BaduContainer.this.markDirty(this);
			}
		}


		private byte[] toByteArray() throws IOException {
			byte[] b = buffer.buffer();
			if (b.length != buffer.length()) {
				b = new byte[(int) buffer.length()];
				System.arraycopy(buffer.buffer(), 0, b, 0, (int) buffer.length());
			}
			return b;
		}

		@Override
		public StorageStrategyHints storageHints() {
			return new StorageStrategyHints() {

				@Override
				public void reliable(boolean reliable) {
				}

				@Override
				public void lifeExpectancy(long timeInMillis) {
				}

				@Override
				public void expirationDate(long timeInMillis) {
				}

				@Override
				public void expectedUpdateFrequency(long intervalInMillis) {
				}

				@Override
				public void expectedSize(int size) {
//					buffer = new RandomAccessByteArray(size);
				}

				@Override
				public void expectedReadFrequency(long intervalInMillis) {
				}
			};
		}

		@Override
		public void remove() throws IOException {
			db.singleInsert(0, serializedId, null, null);
		}

		@Override
		public boolean isEmpty() {
			return (result() == null) || asBuffer().isEmpty();
		}

		private byte[] result() {
			if (result != null) {
				return result;
			}
			try {
				result = db.lookup(0, serializedId, null).get();
			} catch (BabuDBException e) {
				throw new RuntimeException(e);
			}
			return result;
		}

		@Override
		public <TT> Container<TT> asContainer(final Serializer<TT> idSerializerChild) {

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			NamedDataOutput out = new NamedDataOutputStream(bout);
			try {
				idSerializerParent.serialize(out, id);
				out.writeByte("type", 2);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			final byte[] containerSlotId = bout.toByteArray();

			return new BaduContainer<TT>(db, new Serializer<TT>() {

				@Override
				public void serialize(NamedDataOutput out, TT item) throws IOException {
					out.write("parentId", containerSlotId);
					idSerializerChild.serialize(out, item);
				}

				@Override
				public TT deserialize(NamedDataInput in) throws IOException {
					in.skip("parentId", containerSlotId.length);
					return idSerializerChild.deserialize(in);
				}
			}, containerSlotId, this);
		}

		@Override
		public Streamer asStreamer() {
			return new Streamer() {

				@Override
				public NamedDataOutput openOutput(boolean append) {
					BaduContainer.this.markDirty(BaduSlot.this);
					try {
						if (!append) {
							if (buffer == null) {
								buffer = new RandomAccessByteArray();
							} else {
								if (buffer.length() > 0) {
									buffer.position(0);
								}
							}
						} else {
							asBuffer();
							buffer.position(buffer.length());
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return new NamedDataOutputAdapter(buffer);
				}

				@Override
				public NamedDataInput openInput() {
					if (buffer != null) {
						return new NamedDataInputStream(new ByteArrayInputStream(buffer.buffer()));
					}
					if (result() != null) {
						return new NamedDataInputStream(new ByteArrayInputStream(result()));
					}
					return EMPTY_NAMED_DATA_INPUT_STREAM;
				}
			};
		}

		@Override
		public RandomAccessBuffer asBuffer() {
			if (buffer != null) {
				return buffer;
			}

			BaduContainer.this.markDirty(this);
			if (result() == null) {
				return buffer = new RandomAccessByteArray() {
					@Override
					public void flush() throws IOException {
						BaduSlot.this.flush();
					}
				};
			}
			return buffer = new RandomAccessByteArray(result()) {
				@Override
				public void flush() throws IOException {
					BaduSlot.this.flush();
				}
			};
		}

	}
	
	private Serializer<T> idSerializer;
	private Database db;
	private byte[] containerSlotSerializedId;
	private Set<BaduFlushable> dirty = new HashSet<BaduFlushable>();
	private final BaduFlusher parentFlusher;

	public BaduContainer(Database db, Serializer<T> idSerializer) throws BabuDBException {
		this(db, idSerializer, null, EMPTY_FLUSHER);
	}

	public BaduContainer(Database db, Serializer<T> idSerializer, byte[] containerSlotId, BaduFlusher parentFlusher) {
		this.db = db;
		this.idSerializer = idSerializer;
		this.containerSlotSerializedId = containerSlotId;
		this.parentFlusher = parentFlusher;
	}

	public static Database createBaduDB(String name) throws BabuDBException {
		BabuDB databaseSystem = BabuDBFactory.createBabuDB(new BabuDBConfig("babudb/databases/", "babudb/dblog/", 4, 1024 * 1024 * 16, 5 * 60, SyncMode.FSYNC,
																			50, 0, false, 16, 1024 * 1024 * 512));
		DatabaseManager dbm = databaseSystem.getDatabaseManager();
		Database db = dbm.getDatabases().get(name);
		return db != null ? db : dbm.createDatabase(name, 1);
	}

	@Override
	public void flush() throws IOException {
		DatabaseInsertGroup ig = db.createInsertGroup();
		if (flushTo(ig)) {
			try {
				db.insert(ig, null).get();
			} catch (BabuDBException e) {
				throw new RuntimeException(e);
			}
		}
		parentFlusher.markClean(this);
	}
	
	@Override
	public boolean flushTo(DatabaseInsertGroup ig) throws IOException {
		if (dirty.isEmpty()) {
			return false;
		}
		boolean ret = false;
		for(BaduFlushable flushable : dirty) {
			ret |= flushable.flushTo(ig);
		}
		dirty.clear();
		return ret;
	}
	
	@Override
	public void markClean(BaduFlushable flushable) {
		dirty.remove(flushable);
		if (dirty.isEmpty()) {
			parentFlusher.markClean(this);
		}
	}
	
	@Override
	public void markDirty(BaduFlushable flushable) {
		if (flushable == null) {
			System.out.println("---> here");
		}
		dirty.add(flushable);
		if (dirty.size() == 1) {
			parentFlusher.markDirty(this);
		}
	}
	

	@Override
	public void close() throws IOException {
		try {
			db.shutdown();
		} catch (BabuDBException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Slot get(final T id) {
		byte[] serializedId = serializeId(id);
		return new BaduSlot(id, serializedId, idSerializer);
	}

	private byte[] serializeId(final T id) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		NamedDataOutput out = new NamedDataOutputStream(bout);
		try {
			idSerializer.serialize(out, id);
			out.writeByte("type", 1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return bout.toByteArray();
	}

	@Override
	public boolean remove(Slot item) {
		return false;
	}

	@Override
	public T getId(Slot item) {
		return null;
	}

	@Override
	public Serializer<T> idSerializer() {
		return idSerializer;
	}

	@Override
	public void accept(SlotVisitor<T> visitor) {
		DatabaseRequestResult<ResultSet<byte[], byte[]>> r = db.prefixLookup(0, containerSlotSerializedId, null);
		ResultSet<byte[], byte[]> l;
		try {
			l = r.get();
		} catch (BabuDBException e) {
			throw new RuntimeException(e);
		}
		while (l.hasNext()) {
			Entry<byte[], byte[]> next = l.next();
			NamedDataInput in = new NamedDataInputStream(new ByteArrayInputStream(next.getKey()));
			T id;
			try {
				id = idSerializer.deserialize(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			visitor.visit(id, new BaduSlot(id, next.getKey(), idSerializer, next.getValue()));
		}
	}


}
