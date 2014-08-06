package com.udb4o.refimpl.primitive;

import java.io.*;
import java.util.*;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

class PrimitiveSlot implements Slot, Streamer {

	private final class SlotOutputStream extends OutputStream {
		byte[] singleByteBuffer = new byte[1];

		@Override
		public void write(final int b) throws IOException {
			singleByteBuffer[0] = (byte) b;
			write(singleByteBuffer, 0, 1);
		}

		@Override
		public void write(final byte[] buf, int off, int len) throws IOException {

			if (!dirty) {
				markDirty();
			}
			while (len > 0) {
				allocateBuffer();
				if (length == 0 && len >= FlatUUIDContainer.DEFAULT_BLOCK_SIZE) {
					len -= writeBlock(buf, off, len);
				} else if (len <= buffer.length - length) {
					System.arraycopy(buf, off, buffer, length, len);
					length += len;
					len = 0;
				} else {
					final int toCopy = buffer.length - length;
					if (toCopy != 0) {
						System.arraycopy(buf, off, buffer, length, toCopy);
						off += toCopy;
						len -= toCopy;
						length += toCopy;
					}
					writeCurrentBuffer();
				}
			}

		}

		@Override
		public void flush() throws IOException {
			flushInternal();
		}
	}

	private final class SlotInputStream extends InputStream {
		private final byte[] singleByteBuffer = new byte[1];

		@Override
		public int read() throws IOException {
			final int read = read(singleByteBuffer, 0, 1);
			return read == 1 ? singleByteBuffer[0] & 0xff : -1;
		}

		public int read(final byte[] buf, final int off, final int len) throws IOException {
			if (length == 0 && !feedBuffer()) {
				return -1;
			}
			allocateBuffer();

			final int read = Math.min(len, length);
			System.arraycopy(buffer, offset, buf, off, read);
			offset += read;
			length -= read;
			return read;

		}

		protected boolean feedBuffer() throws IOException {
			if (currentBlock == null) {
				if (first == null) {
					return false;
				}
				resetCurrentBlockTo(first);
			} else if (currentBlockOffset == currentBlock.getLength()) {
				final Block next = currentBlock.getNext();
				if (next == null) {
					return false;
				}
				resetCurrentBlockTo(next);
			}
			allocateBuffer();
			length = Math.min(buffer.length, currentBlock.getLength() - currentBlockOffset);
			if (length == 0) {
				throw new RuntimeException("length shouldnt be zero here");
			}
			container().position(currentBlock.getOffset() + currentBlockOffset);
			container().readFully(buffer, 0, length);
			currentBlockOffset += length;
			offset = 0;
			return true;
		}

		private void resetCurrentBlockTo(final Block block) {
			currentBlock = block;
			currentBlockOffset = 0;
		}
	}

	protected final FlatUUIDContainer flatUuidContainer;
	public UUID id;
	public Block first;
	public Block last;

	protected boolean dirty = false;

	private byte[] buffer = null;
	private int length = 0;
	private int offset = 0;
	private Block currentBlock = null;
	private int currentBlockOffset = 0;

	private NamedDataInput in = null;
	private NamedDataOutput out = null;
	private int capacity = -1;

	public PrimitiveSlot(FlatUUIDContainer flatUUIDContainer, final UUID id, final Block block) {
		flatUuidContainer = flatUUIDContainer;
		this.id = id;
		setFirstBlock(block);
	}

	public void setFirstBlock(final Block block) {
		this.first = block;
		last = first;
		if (last != null) {
			while (last.getNext() != null) {
				last = last.getNext();
			}
		}
	}

	public void setBlocks(final Block first, final Block last) {
		this.first = first;
		this.last = last;

	}

	protected void markDirty() {
		dirty = true;
		flatUuidContainer.addDirtySlot(this);
	}

	@Override
	public void close() throws IOException {
		flush();
	}

	@Override
	public void flush() throws IOException {
		if (dirty) {
			writeCurrentBuffer();
		}
		release();
	}

	private void release() {
		buffer = null;
		out = null;
		in = null;
	}

	public boolean flushInternal() throws IOException {
		if (!dirty) {
			release();
			return false;
		}
		writeCurrentBuffer();

		putSlot();

		dirty = false;
		return true;
	}

	protected void putSlot() throws IOException {
		index().writeByte("putSlot", FlatUUIDContainer.PUT_SLOT);
		flatUuidContainer.idSerializer().serialize(index(), id);
		Block.serializer.serialize(index(), first);
	}

	private final NamedDataOutput index() {
		return flatUuidContainer.index;
	}

	private final RandomAccessBuffer container() {
		return flatUuidContainer.container;
	}

	protected void writeCurrentBuffer() throws IOException {
		if (buffer == null) {
			return;
		}
		int offset = 0;
		while (length > 0) {
			int read = writeBlock(buffer, offset, length);
			length -= read;
			offset += read;
		}
		release();
	}
	
	private void allocateBuffer() {
		if (buffer != null) {
			return;
		}
		buffer = new byte[FlatUUIDContainer.DEFAULT_BLOCK_SIZE];
	}

	protected int writeBlock(final byte[] buf, final int off, final int len) throws IOException {
		if (len == 0) {
			return 0;
		}

		Block current = last;

		if (current == null || current.isFull()) {

			container().position(container().length());
			current = new Block((int) container().position(), 0, capacityForLength(len));
			container().setLength(current.getOffset() + current.getCapacity());

		} else {
			container().position(current.getOffset() + current.getLength());
		}

		final int wrote = Math.min(len, current.getRemaningCapacity());
		container().write(buf, off, wrote);
		current.incLength(wrote);

		if (current != last) {
			if (last == null) {
				first = last = current;
			} else {
				last.setNext(current);
				last = current;
			}
		}
		return wrote;
	}

	private int capacityForLength(final int len) {
		return capacity == -1 ? len : capacity;
	}

	@Override
	public void remove() throws IOException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		return first == null;
	}

	@Override
	public StorageStrategyHints storageHints() {
		return new StorageStrategyHints() {

			@Override
			public void reliable(final boolean reliable) {
			}

			@Override
			public void lifeExpectancy(final long timeInMillis) {
			}

			@Override
			public void expirationDate(final long timeInMillis) {
			}

			@Override
			public void expectedUpdateFrequency(final long intervalInMillis) {
			}

			@Override
			public void expectedSize(final int size) {
				capacity = size;
			}

			@Override
			public void expectedReadFrequency(long intervalInMillis) {
			}
		};
	}

	@Override
	public Streamer asStreamer() {
		return this;
	}

	@Override
	public <T> Container<T> asContainer(final Serializer<T> idSerializer) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public RandomAccessBuffer asBuffer() {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public NamedDataOutput openOutput(final boolean append) {
		if (!append) {
			first = last = null;
		} else {
			try {
				flush();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
		length = 0;
		offset = 0;
		if (out == null) {
			out = new NamedDataOutputStream(new SlotOutputStream());
		}
		return out;
	}

	@Override
	public NamedDataInput openInput() {
		currentBlock = null;
		currentBlockOffset = 0;
		length = 0;
		offset = 0;
		if (in == null) {
			in = new NamedDataInputStream(new SlotInputStream());
		}
		return in;
	}

	@Override
	public String toString() {
		Block next = first;
		int count = 0;
		int length = 0;
		int capacity = 0;
		while (next != null) {
			count++;
			length += next.getLength();
			capacity += next.getCapacity();
			next = next.getNext();
		}
		return "Slot [id=" + id + ";blocks=" + count + ";length=" + length + ";capacity=" + capacity + "]";
	}

	@Override
	public void setOpaque(Object opaque) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Object getOpaque() {
		throw new java.lang.UnsupportedOperationException();
	}
}
