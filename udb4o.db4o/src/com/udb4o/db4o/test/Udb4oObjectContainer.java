package com.udb4o.db4o.test;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import com.db4o.*;
import com.db4o.ext.*;
import com.db4o.query.*;
import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class Udb4oObjectContainer implements ObjectContainer, Serializer<Object> {

	
	private final Container<Byte> rootContainer;
	private final Container<String> systemContainer;
	private final Container<Integer> userContainer;
	
	
	
	private ConcurrentMap<Object, Integer> objectToId = new ConcurrentHashMap<Object, Integer>();
	
	private AtomicInteger nextId;

	private ConcurrentMap<Class<?>, Serializer<?>> serializers;

	public Udb4oObjectContainer(IFile file) {
		
		initSerializers();
		
		rootContainer = new XContainer<Byte>(file.openAsBuffer(), Serializers.eightbit);
		systemContainer = rootContainer.get((byte)1).asContainer(Serializers.string);
		userContainer = rootContainer.get((byte)2).asContainer(Serializers.integer);

		try {
			bootstrap();
		} catch (IOException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	private void initSerializers() {
		serializers = new ConcurrentHashMap<Class<?>, Serializer<?>>(Serializers.serializers);
	}

	private void bootstrap() throws IOException {
		nextId = new AtomicInteger(readSystemInt("nextId", 0));
		
		if (!systemContainer.get("classMetadata").isEmpty()) {
			bootClassMetadata(systemContainer.get("classMetadata").asContainer(Serializers.string));
		}
		
	}
	
	private void bootClassMetadata(Container<String> container) {
		container.accept(new SlotVisitor<String>() {
			@Override
			public void visit(String id, Slot slot) {
				NamedDataInput in = slot.asStreamer().openInput();
				try {
					classMetadata((Class<?>) Class.forName(in.readUTF("class")));
				} catch (ClassNotFoundException e) {
					throw new java.lang.RuntimeException(e);
				} catch (IOException e) {
					throw new java.lang.RuntimeException(e);
				}
			}
		});
	}

	private int readSystemInt(String key, int defaultValue) throws IOException {
		Slot slot = systemContainer.get(key);
		return slot.isEmpty() ? defaultValue : slot.asStreamer().openInput().readInt(null);
	}

	private void putSystemInt(String key, int value) throws IOException {
		Slot slot = systemContainer.get(key);
		slot.asStreamer().openOutput(false).writeInt(null, value);
	}

	@Override
	public void activate(Object obj, int depth) throws Db4oIOException, DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public boolean close() throws Db4oIOException {
		try {
			rootContainer.close();
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
		return true;
	}

	@Override
	public void commit() throws Db4oIOException, DatabaseClosedException, DatabaseReadOnlyException {
		try {
			
			putSystemInt("nextId", nextId.get());
			
			systemContainer.flush();
			userContainer.flush();
		} catch (IOException e) {
			new Db4oIOException(e);
		}
	}

	@Override
	public void deactivate(Object obj, int depth) throws DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void delete(Object obj) throws Db4oIOException, DatabaseClosedException, DatabaseReadOnlyException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public ExtObjectContainer ext() {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <T> ObjectSet<T> queryByExample(Object template) throws Db4oIOException, DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public Query query() throws DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <T> ObjectSet<T> query(Class<T> clazz) throws Db4oIOException, DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <TargetType> ObjectSet<TargetType> query(Predicate<TargetType> predicate) throws Db4oIOException, DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <TargetType> ObjectSet<TargetType> query(Predicate<TargetType> predicate, QueryComparator<TargetType> comparator) throws Db4oIOException,
			DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public <TargetType> ObjectSet<TargetType> query(Predicate<TargetType> predicate, Comparator<TargetType> comparator) throws Db4oIOException,
			DatabaseClosedException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void rollback() throws Db4oIOException, DatabaseClosedException, DatabaseReadOnlyException {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public void store(Object obj) throws DatabaseClosedException, DatabaseReadOnlyException {
		Slot slot = slot(obj);
		NamedDataOutput out = slot.asStreamer().openOutput(false);
		try {
			serialize(out, obj);
			slot.flush();
		} catch (IOException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Serializer<T> serializerFor(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		Serializer<?> serializer = serializers.get(clazz);
		if (serializer != null && !(serializer instanceof ClassMetadata)) {
			throw new RuntimeException("Class " + clazz+ " is not a classmetadata");
		}
		ClassMetadataImpl<T> c = (ClassMetadataImpl<T>) serializer;
		if (c == null) {
			c = new ClassMetadataImpl<T>(classMetadata(clazz.getSuperclass()), clazz, this);
			Serializer<?> old = serializers.putIfAbsent(clazz, c);
			if (old != null) {
				c = (ClassMetadataImpl<T>) old;
			}
		}
		return c;
	}
	
	@SuppressWarnings("unchecked")
	private <T> ClassMetadataImpl<T> classMetadata(Class<T> clazz) {
		return (ClassMetadataImpl<T>) serializers.get(clazz);
	}
	

	private Slot slot(Object obj) {
		return userContainer.get(idFor(obj));
	}

	private Integer idFor(Object obj) {
		Integer id = objectToId.get(obj);
		if (id == null) {
			id = nextId.getAndIncrement();
			objectToId.put(obj, id);
		}
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void serialize(NamedDataOutput out, Object item) throws IOException {
		Class<? extends Object> clazz = item.getClass();
		out.writeBoolean("primitive", clazz.isPrimitive());
		out.writeUTF("class", clazz.getName());
		@SuppressWarnings("rawtypes")
		Serializer s = serializerFor(clazz);
		s.serialize(out, item);
	}

	@Override
	public Object deserialize(NamedDataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

}
