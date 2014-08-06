package com.udb4o.refimpl;

import java.io.*;
import java.util.*;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.refimpl.XContainer.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class XSlot implements Slot {

	private final XContainer<?> container;
	private final UUID hash;
	private Slot storage;
	private final IdSerializer parentIdSerializer;
	private XContainer<?> selfContainer;
	private Object opaque;


	public XSlot(XContainer<?> container, UUID hash, IdSerializer parentIdSerializer) {
		this.container = container;
		this.hash = hash;
		this.parentIdSerializer = parentIdSerializer;
	}

	@Override
	public void flush() throws IOException {
		storage().flush();
	}

	@Override
	public void close() throws IOException {
		storage().close();
	}

	@Override
	public void remove() throws IOException {
		storage().remove();
	}

	@Override
	public boolean isEmpty() {
		return storage().isEmpty();
	}

	@Override
	public Streamer asStreamer() {
		return storage().asStreamer();
	}

	@Override
	public RandomAccessBuffer asBuffer() {
		return storage().asBuffer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Container<T> asContainer(final Serializer<T> idSerializer) {
		if (selfContainer != null) {
			return (Container<T>) selfContainer;
		}
		selfContainer = new XContainer<T>(container.storage(), new Serializer<T>() {

			@Override
			public void serialize(NamedDataOutput out, T item) throws IOException {
				parentIdSerializer.write(out);
				idSerializer.serialize(out, item);
			}

			@Override
			public T deserialize(NamedDataInput in) throws IOException {
				parentIdSerializer.read(in);
				return idSerializer.deserialize(in);
			}
		});
		return (Container<T>) selfContainer;
	}

	@Override
	public StorageStrategyHints storageHints() {
		return storage().storageHints();
	}

	private Slot storage() {
		if (storage == null) {
			storage = container.storage().get(hash);
		}
		return storage;
	}

	@Override
	public void setOpaque(Object opaque) {
		this.opaque = opaque;
	}

	@Override
	public Object getOpaque() {
		return opaque;
	}

}
