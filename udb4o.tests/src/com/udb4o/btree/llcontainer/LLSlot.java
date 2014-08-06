package com.udb4o.btree.llcontainer;

import java.io.*;

import com.udb4o.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public final class LLSlot implements Slot {
	public LLSlot(LongFixedBufferContainer intFixedBufferContainer,
			Integer id) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setOpaque(Object opaque) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getOpaque() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StorageStrategyHints storageHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Streamer asStreamer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Container<T> asContainer(Serializer<T> idSerializer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RandomAccessBuffer asBuffer() {
		// TODO Auto-generated method stub
		return null;
	}
}