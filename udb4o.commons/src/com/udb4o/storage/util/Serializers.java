package com.udb4o.storage.util;

import java.io.*;
import java.util.*;

import com.udb4o.io.*;

public class Serializers {

	public static final Map<Class<?>, Serializer<?>> serializers = new HashMap<Class<?>, Serializer<?>>();

	public static <T> Serializer<T> addSerializer(Serializer<T> serializer, Class<?>... classes) {
		for (Class<?> clazz : classes) {
			serializers.put(clazz, serializer);
		}
		return serializer;
	}

	@SuppressWarnings("unchecked")
	public static <T> Serializer<T> serializerFor(Class<T> t) {
		return (Serializer<T>) serializers.get(t);
	}


	public final static Serializer<UUID> uuid = addSerializer(new Serializer<UUID>() {
		
		public UUID deserialize(NamedDataInput in) throws IOException {
			return new UUID(in.readLong("mostSignificantBits"), in.readLong("leastSignificantBits"));
		}

		public void serialize(NamedDataOutput out, UUID id) throws IOException {
			out.writeLong("mostSignificantBits", id.getMostSignificantBits());
			out.writeLong("leastSignificantBits", id.getLeastSignificantBits());
		}

	}, UUID.class);

	public final static Serializer<Boolean> bool = addSerializer(new Serializer<Boolean>() {
		
		public Boolean deserialize(NamedDataInput in) throws IOException {
			return in.readBoolean("value");
		}

		public void serialize(NamedDataOutput out, Boolean v) throws IOException {
			out.writeBoolean("value", v);
		}

	}, Boolean.class, boolean.class);

	
	public final static Serializer<Integer> integer = addSerializer(new Serializer<Integer>() {

		public void serialize(NamedDataOutput out, Integer item) throws IOException {
			out.writeInt("value", item);
		}

		public Integer deserialize(NamedDataInput in) throws IOException {
			return in.readInt("value");
		}
	}, Integer.class, int.class);

	
	public final static Serializer<Long> sixtyfourbit = addSerializer(new Serializer<Long>() {

		public void serialize(NamedDataOutput out, Long item) throws IOException {
			out.writeLong("value", item);
		}

		public Long deserialize(NamedDataInput in) throws IOException {
			return in.readLong("value");
		}
	}, Long.class, long.class);

	
	public final static Serializer<Byte> eightbit = addSerializer(new Serializer<Byte>() {

		public void serialize(NamedDataOutput out, Byte item) throws IOException {
			out.writeByte("value", item);
		}

		public Byte deserialize(NamedDataInput in) throws IOException {
			return in.readByte("value");
		}
	}, Byte.class, byte.class);

	
	public final static Serializer<String> string = addSerializer(new Serializer<String>() {

		public void serialize(NamedDataOutput out, String item) throws IOException {
			out.writeUTF("utf", item);
		}

		public String deserialize(NamedDataInput in) throws IOException {
			return in.readUTF("utf");
		}
	}, String.class);

	
}
