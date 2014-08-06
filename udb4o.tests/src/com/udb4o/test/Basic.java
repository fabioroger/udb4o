package com.udb4o.test;

import java.io.*;
import java.util.*;

import org.junit.*;

import com.udb4o.objectrepository.*;
import com.udb4o.refimpl.*;
import com.udb4o.storage.util.*;
import com.udb4o.util.file.*;

public class Basic {

	private static final IFile CONTAINER_FILE = new RealFile("container.dat");

	public static class Pilot {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@Test
	public void persistDataTypes() throws IOException {

		ObjectContainer oc = new SimpleObjectContainer(new XContainer<UUID>(CONTAINER_FILE.openAsBuffer(), Serializers.uuid));

		oc.runTransaction(new TransactionalRunnable() {

			@Override
			public void run(final Transaction t) {
				
				DataType dataType = addClass(t, Pilot.class);

				DataObject data = dataType.createInstance();

				data.set("name", "Barrichello");

			}

		});

	}

	private static DataType addClass(final Transaction t, Class<?> class1) {
		new ReflectorClassAnalyzer(class1).accept(new ClassAnalyzerVisitor() {

			private DataType dataType;

			@Override
			public void visit(String name, String superName) {
				dataType = t.createDataType(name);
			}

			@Override
			public void visitField(String name, String type) {
				dataType.addField(name, t.getDataType(type));
			}

		});
		return (DataType) t.getDataType(class1.getName());
	}
}
