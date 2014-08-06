package com.udb4o.refimpl;

import java.io.*;
import java.security.*;
import java.util.*;

import com.udb4o.storage.util.*;

public class Sha1Hasher<T> implements UUIDHasher<T> {

	private MessageDigestOutputStream sha1;
	private final Serializer<T> serializer;
	private byte[] buffer;

	public Sha1Hasher(Serializer<T> serializer) {

		this.serializer = serializer;
		try {
			sha1 = new MessageDigestOutputStream(MessageDigest.getInstance("SHA-1"));
			buffer = new byte[sha1.getDigestLength()];
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UUID hash(T value) {
		update(value);
		return digest();
	}

	public UUID digest() {
		try {
			sha1.digest(buffer, 0, buffer.length);
			
			long m = 0;
			long l = 0;
			for (int i = 0; i < 8; i++) {
				m |= (long)((buffer[i]&0xffl) << (i * 8));
			}
			for (int i = 0; i < 8; i++) {
				l |= (long)((buffer[8+i]&0xffl) << (i * 8));
			}
			
			return new UUID(m, l);
		} catch (DigestException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void update(T value) {
		try {
			
			serializer.serialize(sha1, value);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
