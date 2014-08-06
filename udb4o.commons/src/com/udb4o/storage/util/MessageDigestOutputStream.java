package com.udb4o.storage.util;

import java.io.*;
import java.security.*;

import com.udb4o.io.*;

public class MessageDigestOutputStream extends NamedDataOutputStream {
	
	private final MessageDigest digester;

	public MessageDigestOutputStream(final MessageDigest digester) {
		super(new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				digester.update((byte) b);
			}
			
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				digester.update(b, off, len);
			}
		});
		this.digester = digester;
	}


	public byte[] digest() {
		return digester.digest();
	}

	public int digest(byte[] buf, int offset, int len) throws DigestException {
		return digester.digest(buf, offset, len);
	}

	public byte[] digest(byte[] input) {
		return digester.digest(input);
	}

	public final String getAlgorithm() {
		return digester.getAlgorithm();
	}

	public final int getDigestLength() {
		return digester.getDigestLength();
	}

	public final Provider getProvider() {
		return digester.getProvider();
	}


	public void reset() {
		digester.reset();
	}

}
