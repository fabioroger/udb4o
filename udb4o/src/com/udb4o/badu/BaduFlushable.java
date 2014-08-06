package com.udb4o.badu;

import java.io.*;

import org.xtreemfs.babudb.api.database.*;

public interface BaduFlushable {
	boolean flushTo(DatabaseInsertGroup ig) throws IOException;
}