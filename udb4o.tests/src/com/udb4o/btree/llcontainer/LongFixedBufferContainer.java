package com.udb4o.btree.llcontainer;

import java.io.*;

import com.udb4o.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public final class LongFixedBufferContainer implements Container<Integer> {
	
	private final RandomAccessBuffer storage;
	private RandomAccessBuffer slotBuffer;
	private Slot slot;
	private long slotOffset;
	private long slotId;
	
	public LongFixedBufferContainer(RandomAccessBuffer storage) {
		this.storage = storage;
		init();
	}
	
	private void init() {
		slotBuffer = new DelegatingRandomAccessBuffer(storage) {
			@Override
			public long position() throws IOException {
				return super.position()-slotOffset;
			}
			@Override
			public void position(long newPosition) throws IOException {
				super.position(slotOffset+newPosition);
			}

			@Override
			public long length() throws IOException {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void setLength(long newLength) throws IOException {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public boolean isEmpty() {
				throw new UnsupportedOperationException();
			}
		};
		slot = new Slot() {
			
			@Override
			public void setOpaque(Object opaque) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Object getOpaque() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void close() throws IOException {
			}
			
			@Override
			public void flush() throws IOException {
			}
			
			@Override
			public StorageStrategyHints storageHints() {
				return null;
			}
			
			@Override
			public void remove() throws IOException {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public boolean isEmpty() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Streamer asStreamer() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public <T> Container<T> asContainer(Serializer<T> idSerializer) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public RandomAccessBuffer asBuffer() {
				return slotBuffer;
			}
		};
	}

	@Override
	public void close() throws IOException {
		storage.close();
	}

	@Override
	public void flush() throws IOException {
		storage.flush();
	}

	@Override
	public boolean remove(Slot item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Serializer<Integer> idSerializer() {
		return Serializers.integer;
	}

	@Override
	public Integer getId(Slot item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Slot get(Integer id) {
		try {
			slotId = id;
			slotOffset = slotId;
			storage.position(slotOffset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return slot;
	}

	@Override
	public void accept(SlotVisitor<Integer> visitor) {
		throw new UnsupportedOperationException();
	}
}