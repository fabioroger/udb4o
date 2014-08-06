package com.udb4o.refimpl.primitive;

import java.io.*;

import com.udb4o.io.*;
import com.udb4o.storage.util.*;

public class Block implements Cloneable {
	private final int offset;
	private int capacity;
	private int length;

	private Block next = null;

	public Block(int offset, int length, int capacity) {
		this.offset = offset;
		this.length = length;
		this.capacity = capacity;
	}

	public Block(int offset) {
		this(offset, 0, 0);
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public static final Serializer<Block> serializer = new Serializer<Block>() {

		@Override
		public void serialize(NamedDataOutput out, Block item) throws IOException {

			Block next = item;

			while (next != null) {

				out.writeBoolean("hasNext", true);

				serializeSingleBlock(out, next);

				next = next.getNext();
			}

			out.writeBoolean("hasNext", false);
		}

		@Override
		public Block deserialize(NamedDataInput in) throws IOException {

			Block first = null;
			Block last = null;

			while (in.readBoolean("hasNext")) {
				Block next = deserializeSingleBlock(in);
				if (first == null) {
					first = last = next;
				} else {
					last.next = next;
					last = next;
				}
			}
			return first;
		}

	};

	public static void serializeSingleBlock(NamedDataOutput out, Block next) throws IOException {
		out.writeInt("offset", next.offset);
		out.writeInt("length", next.length);
		out.writeInt("capacity", next.capacity);
	}

	public static Block deserializeSingleBlock(NamedDataInput in) throws IOException {
		return new Block(in.readInt("offset"), in.readInt("length"), in.readInt("capacity"));
	}

	public void incLength(int len) {
		length += len;
	}

	public void setNext(Block next) {
		this.next = next;
	}

	public Block getNext() {
		return next;
	}

	public boolean isFull() {
		return capacity == length;
	}

	public int getRemaningCapacity() {
		return capacity - length;
	}

	@Override
	public String toString() {
		return "Block [offset=" + offset + ", capacity=" + capacity + ", length=" + length + ", next=" + (next == null ? "null" : "not null") + "]";
	}

	public Block clone() {
		Block block = new Block(offset, length, capacity);
		block.next = next;
		return block;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + capacity;
		result = prime * result + length;
		result = prime * result + offset;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (capacity != other.capacity)
			return false;
		if (length != other.length)
			return false;
		if (offset != other.offset)
			return false;
		if (next != other.next)
			return false;
		return true;
	}

}
