package com.udb4o.refimpl.primitive;

import java.io.*;
import java.util.*;

import com.udb4o.util.file.*;

final class IndexSlot extends PrimitiveSlot {
	private Block lastRead = null;
	private int offsetLast = 0;
	private int lengthLast = 0;
	private Block lastReadClone;
	private final RandomAccessBuffer container;

	IndexSlot(FlatUUIDContainer flatUUIDContainer, UUID id, Block block) {
		super(flatUUIDContainer, id, block);
		container = flatUuidContainer.container;
	}
	
	int count = 0;
	
	@Override
	protected int writeBlock(byte[] buf, int off, int len) throws IOException {
		int ret = super.writeBlock(buf, off, len);
		return ret;
	}

	protected void putSlot() throws IOException {

		if (lastReadClone != null && lastReadClone.equals(last)) {
			return;
		}

		container.position(offsetLast + lengthLast);
		Block next = lastRead;
		if (next == null) {
			next = first;
		}
		while (next != null) {
			lastRead = next;
			if (lengthLast + 12 > FlatUUIDContainer.INDEX_BLOCK_SIZE) {

				int nextOffset = (int) container.length();
				container.writeInt(nextOffset);
				container.setLength(nextOffset + FlatUUIDContainer.INDEX_BLOCK_SIZE);
				container.position(nextOffset);
				offsetLast = nextOffset;
				lengthLast = 0;
			}
			container.writeInt(next.getOffset());
			container.writeInt(next.getLength());
			next = next.getNext();
			if (next != null) {
				lengthLast += 8;
			}
		}
		lastReadClone = lastRead.clone();
		container.writeInt(0);
	}

	@Override
	protected void markDirty() {
		dirty = true;
	}

	public void bootstrapIndex() throws IOException {
		int offset;
		int count = 0;
		Block first = null;
		Block last = null;
		offsetLast = 0;
		container.position(0);
		while ((offset = container.readInt()) != 0) {
			if (count + 12 >= FlatUUIDContainer.INDEX_BLOCK_SIZE) {
				container.position(offset);
				offsetLast = offset;
				count = 0;
				continue;
			}
			count += 8;

			int len = container.readInt();
			Block block = new Block(offset, len, FlatUUIDContainer.INDEX_BLOCK_SIZE);
			if (last == null) {
				first = last = block;
			} else {
				last.setNext(block);
				last = block;
			}
		}
		
		lengthLast = count == 0 ? 0 : count - 8;
		lastRead = last;
		setBlocks(first, last);
		if (lastRead != null) {
			lastReadClone = lastRead.clone();
		}
	}

}
