package com.udb4o.storage.util;

import java.io.*;
import java.security.*;
import java.util.*;

public class UUIDUtil {

	public static UUID generateFrom(byte[] bytes) {

		try {
			byte[] ret = hash(bytes);

			long m = 0;
			long l = 0;
			for (int i = 0; i < 8; i++) {
				m |= (long)((ret[i]&0xffl) << (i * 8));
			}
			for (int i = 0; i < 8; i++) {
				l |= (long)((ret[8+i]&0xffl) << (i * 8));
			}
			return new UUID(m, l);

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static MessageDigest sha1;
	
	static {
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] hash(byte[] text) throws UnsupportedEncodingException {
		synchronized (sha1) {
			sha1.reset();
			sha1.update(text, 0, text.length);
			return sha1.digest();
		}
	}

}
