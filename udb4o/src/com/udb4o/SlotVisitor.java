package com.udb4o;

public interface SlotVisitor<T> {
	void visit(T id, Slot slot);
}