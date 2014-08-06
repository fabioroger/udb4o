package com.udb4o.test.reflect;

import java.io.*;
import java.lang.reflect.*;

import org.junit.*;

import com.udb4o.*;
import com.udb4o.io.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class Basic {

	IFile file = new RealFile("test.dat");

	@Before
	@After
	public void cleanup() {
		if (file.exists()) {
			file.delete();
		}
	}

	public static class Pilot {
		
		private String name;
		private int points;
		
		public Pilot() {
		}

		public Pilot(String name, int points) {
			super();
			this.name = name;
			this.points = points;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPoints() {
			return points;
		}

		public void setPoints(int points) {
			this.points = points;
		}

	}

	public static class Reflective{

		public static <T> void save(Slot slot, T obj) throws IllegalArgumentException, IllegalAccessException, IOException {
			
			Container<String> oc = slot.asContainer(Serializers.string);
			NamedDataOutput fields = oc.get("fields").asStreamer().openOutput(false);
			Container<String> c = oc.get("data").asContainer(Serializers.string);
			
			Field[] fs = obj.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				
				Class<?> t = f.getType();
				
				Object v = f.get(obj);
				
				if (v == null) continue;
				
				Slot fieldSlot = c.get(f.getName());
				NamedDataOutput out = fieldSlot.asStreamer().openOutput(false);
				
				@SuppressWarnings("unchecked")
				Serializer<Object> s = (Serializer<Object>) Serializers.serializerFor(t);
				if (s == null) {
					throw new UnsupportedOperationException("Type '"+t+"' not supported");
				}
				fields.writeBoolean("hasNext", true);
				fields.writeUTF("field", f.getName());
				s.serialize(out.nest(f.getName()), v);
				fieldSlot.flush();
			}
			fields.writeBoolean("hasNext", false);
			c.flush();
			
			
		}

		public static <T> T populate(Slot slot, final T obj) throws SecurityException, NoSuchFieldException, IOException, IllegalArgumentException, IllegalAccessException {
			
			Container<String> oc = slot.asContainer(Serializers.string);
			NamedDataInput fields = oc.get("fields").asStreamer().openInput();
			Container<String> c = oc.get("data").asContainer(Serializers.string);
			
			while(fields.readBoolean("hasNext")) {
				
				Field f = obj.getClass().getDeclaredField(fields.readUTF("field"));
				f.setAccessible(true);
				
				NamedDataInput in = c.get(f.getName()).asStreamer().openInput();
				
				Class<?> t = f.getType();
				@SuppressWarnings("unchecked")
				Serializer<Object> s = (Serializer<Object>) Serializers.serializerFor(t);
				if (s == null) {
					throw new UnsupportedOperationException("Type '"+t+"' not supported");
				}
				f.set(obj, s.deserialize(in.nest(f.getName())));
			}
			
			return obj;
		}
		
	}
	
	@Test
	public void basic() throws IOException, IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {

		Container<String> container = new XContainer<String>(file.openAsBuffer(), Serializers.string);

		Reflective.save(container.get("one"), new Pilot("Barichello", 42));
		
		container.flush();
		
		container.close();

		
		container = new XContainer<String>(file.openAsBuffer(), Serializers.string);


		Pilot p = Reflective.populate(container.get("one"), new Pilot());
		
		Assert.assertEquals(42, p.getPoints());
		Assert.assertEquals("Barichello", p.getName());
		
		container.close();

		
	}

}
