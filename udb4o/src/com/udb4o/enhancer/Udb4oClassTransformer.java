package com.udb4o.enhancer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Udb4oClassTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
			throws IllegalClassFormatException {

		if (loader == null) {
			return null;
		}
		if (isIgnored(className)) {
			return null;
		}

		System.out.println("---> " + className);

		return null;
	}

	private boolean isIgnored(String className) {
		return className.startsWith("java/") || className.startsWith("org/") || className.startsWith("sun/") || className.startsWith("net/")
				|| className.startsWith("gnu/") || className.startsWith("javax/") || className.startsWith("com/sun/");
	}
}
