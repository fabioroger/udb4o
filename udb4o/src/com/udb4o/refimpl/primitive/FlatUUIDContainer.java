package com.udb4o.refimpl.primitive;

import java.io.*;
import java.util.*;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class FlatUUIDContainer implements Container<UUID> {

	static final int DEFAULT_BLOCK_SIZE = 1024;

	static final int INDEX_BLOCK_SIZE = 1024;

	private static final int INDEX_SNAPSHOT = 1;
	static final int PUT_SLOT = 2;

	RandomAccessBuffer container;
	NamedDataOutput index;

	private static final UUID INDEX_ID = new UUID(892357873908234l, -8237896293849624l);

	private Map<UUID, PrimitiveSlot> slots = new HashMap<UUID, PrimitiveSlot>();

	private Collection<PrimitiveSlot> dirtySlots = new ArrayList<PrimitiveSlot>();

	private IndexSlot indexSlot;

	public FlatUUIDContainer(RandomAccessBuffer container) {
		this.container = container;

		indexSlot = new IndexSlot(this, INDEX_ID, null);
		indexSlot.storageHints().expectedSize(INDEX_BLOCK_SIZE);
		slots.put(INDEX_ID, indexSlot);

		try {
			if (container.isEmpty()) {
				resetBootstrap();
			} else {
				indexSlot.bootstrapIndex();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			initialize(indexSlot.asStreamer().openInput());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		index = indexSlot.asStreamer().openOutput(true);
	}

	private void resetBootstrap() throws IOException {
		container.writeInt(0);
		container.setLength(INDEX_BLOCK_SIZE);
	}

	public void addDirtySlot(PrimitiveSlot slot) {
		dirtySlots.add(slot);
	}

	private void initialize(NamedDataInput in) throws IOException {
		try {
			while (true) {
				readOp(in);
			}
		} catch (EOFException e) {
		}
	}

	private void readOp(NamedDataInput in) throws IOException {
		int op = in.readByte("operation");
		switch (op) {
		case INDEX_SNAPSHOT:
			readSnapshot(in);
			break;
		case PUT_SLOT:
			readPutSlot(in);
			break;
		default:
			throw new IllegalStateException("Unknown operation: " + op);	
		}
	}

	private void readPutSlot(NamedDataInput in) throws IOException {
		UUID id = idSerializer().deserialize(in);
		get(id).setFirstBlock(Block.serializer.deserialize(in));
	}

	private void readSnapshot(NamedDataInput in) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void flush() throws IOException {
		boolean dirty = false;
		for (PrimitiveSlot slot : dirtySlots) {
			dirty |= slot.flushInternal();
		}
		if (!dirty) {
			return;
		}
		indexSlot.flushInternal();
		dirtySlots.clear();
		container.flush();
	}

	@Override
	public void close() throws IOException {
		flush();
		container.close();
	}

	@Override
	public PrimitiveSlot get(UUID id) {
//		System.out.println("       slot: "+ id);
		PrimitiveSlot slot = slots.get(id);
		if (slot == null) {
			slot = new PrimitiveSlot(this, id, null);
			slots.put(id, slot);
		}
		return slot;
	}

	@Override
	public boolean remove(Slot item) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public UUID getId(Slot item) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Serializer<UUID> idSerializer() {
		return Serializers.uuid;
	}

	@Override
	public void accept(SlotVisitor<UUID> visitor) {
		throw new java.lang.UnsupportedOperationException();
	}

}
