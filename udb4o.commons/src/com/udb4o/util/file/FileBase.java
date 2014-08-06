package com.udb4o.util.file;

import java.io.*;
import java.util.jar.*;

public abstract class FileBase implements IFile {
	
	@Override
	public XMLParser xml() {
		return new XMLParserImpl(this);
	}
	
	@Override
	public PrintWriter openPrintWriter(boolean append) {
		return new PrintWriter(openOutputStream(append));
	}

    @Override
    public int copyFrom(InputStream in) throws IOException {
        OutputStream out = openOutputStream(false);
        int total = copyFully(in, out);
        out.flush();
        out.close();
        return total;
    }

    @Override
    public IFile createFile(String name, InputStream in) throws IOException {
        IFile file = file(name);
        file.copyFrom(in);
        return file;
    }
    
    @Override
    public void jarTo(IFile file) throws IOException {
        final JarOutputStream out = new JarOutputStream(file.openOutputStream(false), new Manifest());
        
        accept(new FileVisitor() {

            @Override
            public void visit(IFile child) {
                
                if (child.isDirectory()) {
                    child.accept(this);
                    return;
                }

                try {
                    String relativePath = child.getRelativePathTo(FileBase.this);

                    JarEntry jarAdd = new JarEntry(relativePath);
                    jarAdd.setTime(child.lastModified());
                    out.putNextEntry(jarAdd);

                    child.copyTo(out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        out.flush();
        out.close();
    }
    
    @Override
    public void unjarTo(IFile dir) throws IOException {
        
        JarInputStream jar = new JarInputStream(openInputStream());
        
        JarEntry entry;
        while((entry = jar.getNextJarEntry()) != null) {
            IFile file = dir.file(entry.getName());
            file.copyFrom(jar);
            file.setLastModified(entry.getTime());
        }
        
        jar.close();
        
    }

    
    public int copyFully(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		int read;
		int total = 0;
		while((read=in.read(buffer))!=-1) {
			out.write(buffer, 0, read);
			total += read;
		}
		return total;
	}
    
    public int copyFullyAndClose(InputStream in, OutputStream out) throws IOException {
    	int total = copyFully(in, out);
    	out.flush();
    	out.close();
    	in.close();
		return total;
    }
    
	@Override
	public BufferedReader openBufferedReader() {
		return new BufferedReader(new InputStreamReader(openInputStream()));
//		try {
//			return new BufferedReader(new InputStreamReader(openInputStream(), "UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException(e);
//		}
	}
	
	@Override
	public void delete() {
		if (isDirectory()) {
			throw new IllegalArgumentException("For directories you must call delete with a boolean argument");
		}
		delete(false);
	}

	@Override
	public void copyTo(IFile dir) throws IOException {
		dir.createFile(name(), openInputStream());
	}

	@Override
	public String getContent() {
		return new String(readFully());
	}
}
