package com.udb4o.badu;

public interface BaduFlusher {
	void markDirty(BaduFlushable flushable);
	void markClean(BaduFlushable flushable);
}