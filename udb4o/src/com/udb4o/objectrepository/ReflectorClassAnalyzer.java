package com.udb4o.objectrepository;

import java.lang.reflect.Field;

@SuppressWarnings("rawtypes")
public class ReflectorClassAnalyzer {

	private final Class clazz;

	public ReflectorClassAnalyzer(Class clazz) {
		this.clazz = clazz;
	}

	public void accept(ClassAnalyzerVisitor visitor) {

		visitor.visit(clazz.getName(), clazz.getSuperclass().getName());

		Field[] fs = clazz.getDeclaredFields();
		for (Field f : fs) {
			visitor.visitField(f.getName(), f.getType().getName());
		}

	}

}
