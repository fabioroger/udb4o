package com.udb4o.util.file;

import java.io.*;
import java.util.*;

public class MemoryFile extends FileBase {

	private MemoryFile parent;
	private final String fileName;

	private byte[] content;
	private int length = 0;
	private Map<String, MemoryFile> children;
	private FileType type = FileType.None;
	private long lastModified = System.currentTimeMillis();
    private File realFile = null;

	enum FileType {
		None, File, Dir
	}

	public MemoryFile(MemoryFile parent, String file) {
		this.parent = parent;
		this.fileName = file;
	}

	public MemoryFile(String file) {
		this((MemoryFile)null, file);
	}

	public MemoryFile() {
		this((MemoryFile)null, "root");
	}

    public MemoryFile(String string, byte[] content) {
        this(string);
        setContent(content);
    }

    @Override
	public String toString() {
		return "MemoryFile[" + getAbsolutePath() + "]";
	}

	public IFile file(String name) {

		int t = name.indexOf('/');

		if (t != -1) {
			String first = name.substring(0, t);
			return (t == 0 ? this : file(first)).file(name.substring(t + 1));
		}

		if ("..".equals(name)) {
			return parent();
		}

		MemoryFile file = children().get(name);

		if (file == null) {
			file = new MemoryFile(this, name);
			children().put(name, file);
		}

		return file;
	}

	public InputStream openInputStream() {
		return new ByteArrayInputStream(content, 0, length);
	}

	public String getAbsolutePath() {
		try {
			return nativeFile().getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String name() {
		return fileName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
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
		MemoryFile other = (MemoryFile) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		return true;
	}

	private Map<String, MemoryFile> children() {
		if (children == null) {
			mkdir();
			children = new HashMap<String, MemoryFile>();
		}
		return children;
	}

	public OutputStream openOutputStream(boolean append) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream() {
			@Override
			public void flush() throws IOException {
				super.flush();
				content = toByteArray();
				length = content.length;
				lastModified = System.currentTimeMillis();
			}
		};
		if (append && content != null) {
			buffer.write(content, 0, length);
		}
		mkfile();
		parent().mkdir();
		return buffer;
	}

	private void mkfile() {
		if (type == FileType.Dir) {
			throw new RuntimeException("Path is already a directory");
		}
		parent.mkdir();
		type = FileType.File;
	}

	public void mkdir() {
		if (type == FileType.File) {
			throw new RuntimeException("Path is already a file");
		}
		type = FileType.Dir;
	}

	public boolean exists() {
		return type != FileType.None;
	}

	public MemoryFile parent() {
		return parent;
	}

	public boolean exists(String fileName) {
		return children != null && children.containsKey(fileName);
	}

	public void accept(FileVisitor visitor) {
		accept(visitor, 0xffffffff);
	}

	public void accept(FileVisitor visitor, final int visitorOptions) {
		if (children == null)
			return;

		for (MemoryFile f : children.values()) {
			if (contains(visitorOptions, f.isFile() ? FileVisitor.FILE : FileVisitor.DIRECTORY)) {
				visitor.visit(f);
			}
		}
	}

	public boolean isDirectory() {
		return type == FileType.Dir;
	}

	public boolean isFile() {
		return type == FileType.File;
	}

	public String getRelativePathTo(IFile base) {
		throw new java.lang.UnsupportedOperationException();
	}

	public long lastModified() {
		return lastModified;
	}

	public int copyTo(OutputStream out) throws IOException {
		out.write(content, 0, length);
		return length;
	}

	public File nativeFile() {
        
        if (realFile != null) {
            return realFile;
        }
        
        try {
            
            if (parent != null) {
                realFile = new File(parent.nativeFile(), fileName);
            } else {
                realFile = File.createTempFile(fileName, ".tmp");
            }
            
            if (isDirectory()) {
                if (realFile.exists()) {
                    realFile.delete();
                }
                realFile.mkdirs();
                realFile.deleteOnExit();
                return realFile;
            }

            OutputStream out = new FileOutputStream(realFile);
            out.write(content, 0, length);
            out.flush();
            out.close();
            
            realFile.deleteOnExit();
            
            return realFile;
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }
	
    public static boolean contains(int haystack, int needle) {
        return (haystack & needle) != 0;
    }

    public byte[] readFully() {
        byte[] buffer = new byte[length];
        System.arraycopy(content, 0, buffer, 0, length);
        return buffer;
    }

    public void delete(boolean recursive) {
        content = null;
        length = 0;
        type = FileType.None;
        children = null;
    }

    public IFile setContent(String content) {
        return setContent(content.getBytes());
    }

    IFile setContent(byte[] bytes) {
        this.content = bytes;
        length = bytes.length;
        type = FileType.File;
        return this;
    }

    public void touch() {
        setContent("");
    }
    
    @Override
    public void setLastModified(long time) {
        lastModified = time;
    }

	@Override
	public RandomAccessBuffer openAsBuffer() {
		if (content == null) {
			content = new byte[1024];
		}
		return new RandomAccessByteArray(content, length) {
			@Override
			public void flush() throws IOException {
				content = Arrays.copyOf(buffer(), (int) length());
				length = (int) length();
			}
		};
	}
	
	@Override
	public boolean isEmpty() {
		return length == 0 && (children == null || children.isEmpty());
	}

}
