package com.udb4o.db4o.test;

import java.io.*;
import java.lang.reflect.*;

import com.udb4o.io.*;
import com.udb4o.storage.util.*;

final class ClassMetadataImpl<T> implements ClassMetadata<T>, Serializer<T> {
	
//	private final Class<? extends T> clazz;
	private final ClassMetadataImpl<? super T> parent;
	private Field[] fields;
	private final Serializer<Object> serializer;

	public ClassMetadataImpl(ClassMetadataImpl<? super T> parent, Class<? extends T> clazz, Serializer<Object> serializer) {
		this.parent = parent;
//		this.clazz = clazz;
		this.serializer = serializer;
		fields = clazz.getDeclaredFields();
	}

	@Override
	public Serializer<T> serializer() {
		return this;
	}

	@Override
	public void serialize(NamedDataOutput out, T item) throws IOException {
		if (parent != null) {
			parent.serialize(out, item);
		}
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				Object v = f.get(item);
				if (v == null) {
					continue;
				}
				NamedDataOutput fout = out.nest(f.getName());
				fout.writeUTF("name", f.getName());
				fout.writeUTF("type", f.getType().getName());
				serializer.serialize(out.nest("data"), v);
			} catch (IllegalArgumentException e) {
				throw new java.lang.RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new java.lang.RuntimeException(e);
			}
		}
	}

	@Override
	public T deserialize(NamedDataInput in) throws IOException {
		throw new java.lang.UnsupportedOperationException();
	}
}