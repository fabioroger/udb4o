package com.udb4o.util.file;

import java.io.*;

public class RealFile extends FileBase {

	private RealFile parent;
	private final File realFile;

	public RealFile(RealFile parent, File file) {
		this.parent = parent;
		this.realFile = file;
	}

	public RealFile(String name) {
		this(new File(name));
	}

	public RealFile(File file) {
		this(null, file);
	}

	@Override
	public String toString() {
		return getAbsolutePath();
	}

	public IFile file(String name) {

		int t = name.indexOf('/');
		
		if (t == 0) {
			return root().file(name.substring(1));
		}

		if (t != -1) {
			String first = name.substring(0, t);
			return (t == 0 ? this : file(first)).file(name.substring(t + 1));
		}

		if ("..".equals(name)) {
			return parent();
		}

		return file(new File(realFile, name));
	}

	private RealFile root() {
		return new RealFile(new File("/"));
	}

	private RealFile file(File file) {
		return new RealFile(this, file);
	}

	public InputStream openInputStream() {
		mkParents();
		try {
			return new FileInputStream(realFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void mkParents() {
		if (parent != null) {
			parent.mkdir();
		}
	}

	public void mkdir() {
		if (parent != null) {
			parent.mkdir();
		}
		if (!realFile.exists()) {
			realFile.mkdirs();
		}
	}

	public String getAbsolutePath() {
		try {
			return realFile.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String name() {
		return realFile.getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((realFile == null) ? 0 : realFile.hashCode());
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
		RealFile other = (RealFile) obj;
		if (realFile == null) {
			if (other.realFile != null)
				return false;
		} else if (!realFile.equals(other.realFile))
			return false;
		return true;
	}

	public OutputStream openOutputStream(boolean append) {
		mkParents();

		try {
			return new FileOutputStream(realFile, append);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean exists() {
		return realFile.exists();
	}

	public IFile parent() {
		if (parent == null) {
			File parentFile = realFile.getAbsoluteFile().getParentFile();
			if (parentFile != null) {
				parent = new RealFile(parentFile);
			}
		}
		return parent;
	}

	public boolean exists(String fileName) {
		return new File(realFile, fileName).exists();
	}

	public void accept(final FileVisitor visitor) {
		accept(visitor, 0xffffffff);
	}

	public void accept(final FileVisitor visitor, final int visitorOptions) {
		realFile.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (MemoryFile.contains(visitorOptions, file.isFile() ? FileVisitor.FILE : FileVisitor.DIRECTORY)) {
					visitor.visit(file(file));
				}
				return false;
			}
		});
	}

	public boolean isDirectory() {
		return realFile.isDirectory();
	}

	public boolean isFile() {
		return realFile.isFile();
	}

	public String getRelativePathTo(IFile base) {
		String baseAbsolutePath = base.getAbsolutePath();
		String absolutePath = getAbsolutePath();
		if (baseAbsolutePath.length() == absolutePath.length()) {
			return "";
		}
		return absolutePath.substring(baseAbsolutePath.length()+1);
	}

	public long lastModified() {
		return realFile.lastModified();
	}

	public int copyTo(OutputStream out) throws IOException {
		InputStream in = openInputStream();
		try {
			return copyFully(in, out);
		} finally {
			in.close();
		}
	}

	public File nativeFile() {
		return realFile;
	}

    public byte[] readFully() {
        
        if (!realFile.exists()) {
            return null;
        }
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[8192];
            int read;
            InputStream in = openInputStream();
            while((read=in.read(buffer))!=-1) {
                out.write(buffer, 0, read);
            }
            
            out.flush();
            in.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(final boolean recursive) {
        if (isDirectory()) {
            accept(new FileVisitor() {
                public void visit(IFile child) {
                    if (child.isDirectory()) {
                        if (recursive) {
                            child.accept(this);
                        }
                    }
                    child.delete(false);
                }
            });
        }
        realFile.delete();
    }

    public IFile setContent(String content) {
        setContent(content.getBytes());
        return this;
    }

    private void setContent(byte[] bytes) {
        
        OutputStream out = openOutputStream(false);
        try {
            out.write(bytes);
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    public void touch() {
        setContent("");
    }
    
    public static RealFile tempDir() {
        try {
            File f = File.createTempFile(RealFile.class.getName(), "temp");
            f.delete();
            if (!f.mkdir()) {
                throw new RuntimeException("Couldnt create temporary dir: " + f.getAbsolutePath());
            }
            f.deleteOnExit();
            return new RealFile(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public RandomAccessBuffer openAsBuffer() {
		mkParents();

		try {
			return new RandomAccessRealFile(nativeFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean isEmpty() {
		return !nativeFile().exists() || nativeFile().length() == 0;
	}

	@Override
    public void setLastModified(long time) {
        nativeFile().setLastModified(time);
    }
}
