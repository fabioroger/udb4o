package com.udb4o.refimpl;

import java.io.*;
import java.util.*;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.refimpl.primitive.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class XContainer<T> implements Container<T> {

	private final Serializer<T> idSerializer;
	private final FlatUUIDContainer storage;
	private final UUIDHasher<T> hasher;
	private final Map<T, XSlot> slots = new HashMap<T, XSlot>();

	public XContainer(RandomAccessBuffer root, Serializer<T> idSerializer) {
		this(new FlatUUIDContainer(root), idSerializer);
	}

	public XContainer(FlatUUIDContainer storage, Serializer<T> idSerializer) {
		this.storage = storage;
		this.idSerializer = idSerializer;
		hasher = new Sha1Hasher<T>(idSerializer);
	}

	@Override
	public void flush() throws IOException {
		storage().flush();
	}

	@Override
	public void close() throws IOException {
		storage.close();
	}

	public interface IdSerializer {
		void write(NamedDataOutput out) throws IOException;
		void read(NamedDataInput in) throws IOException;
	}
	
	@Override
	public Slot get(final T id) {
		XSlot slot = slots.get(id);
		if (slot != null) {
			return slot;
		}
		UUID hash = hasher.hash(id);
		slot = new XSlot(this, hash, new IdSerializer() {
			
			@Override
			public void write(NamedDataOutput out) throws IOException {
				idSerializer().serialize(out.nest("parentId"), id);
			}

			@Override
			public void read(NamedDataInput in) throws IOException {
				idSerializer().deserialize(in.nest("parentId"));
			}
		});
		slots.put(id, slot);
		return slot;
	}
	
	public FlatUUIDContainer storage() {
		return storage;
	}


	@Override
	public boolean remove(Slot item) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public T getId(Slot item) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Serializer<T> idSerializer() {
		return idSerializer;
	}

	@Override
	public void accept(SlotVisitor<T> visitor) {
		throw new java.lang.UnsupportedOperationException();
	}

}
