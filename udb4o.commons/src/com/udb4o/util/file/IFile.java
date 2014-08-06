package com.udb4o.util.file;

import java.io.*;

public interface IFile extends FileOpener {

	IFile createFile(String name, InputStream in) throws IOException;

	InputStream openInputStream();
	OutputStream openOutputStream(boolean append);

	String getAbsolutePath();

	String name();

	boolean exists();
	boolean exists(String fileName);

	IFile parent();
	
	void mkdir();

	void accept(FileVisitor visitor);
	void accept(FileVisitor visitor, int visitorOptions);

	boolean isDirectory();

	boolean isFile();

	String getRelativePathTo(IFile base);

	long lastModified();

	int copyTo(OutputStream out) throws IOException;

	File nativeFile();
	
	byte[] readFully();

    void delete();
    void delete(boolean recursive);

    IFile setContent(String content);
    
    int copyFrom(InputStream in) throws IOException;

    void touch();

	XMLParser xml();

	BufferedReader openBufferedReader();
	
	RandomAccessBuffer openAsBuffer();
	
	PrintWriter openPrintWriter(boolean append);

	boolean isEmpty();
	
	void copyTo(IFile dir) throws IOException;

	String getContent();
	
    void jarTo(IFile file) throws IOException;

    void unjarTo(IFile dir) throws IOException;

    void setLastModified(long time);
    
}
