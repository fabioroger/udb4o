package com.udb4o.objectrepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectContainerFactory {

	private static final class TransactionImpl implements Transaction {
		private Map<String, DataType> dataTypes = new HashMap<String, DataType>();

		@Override
		public DataType createDataType(String name) {
			DataType t = new DataType() {

				private Map<String, DataField> fields = new LinkedHashMap<String, DataField>();

				@Override
				public DataObject createInstance() {
					return new DataObject() {

						private Map<DataField, Object> values = new HashMap<DataField, Object>();

						@Override
						public void set(DataField field, Object value) {
							values.put(field, value);
						}

						@Override
						public void set(String name, String value) {
							values.put(fields.get(name), value);
						}
					};
				}

				@Override
				public DataField addField(String name, Type fieldType) {
					DataField df = new DataField() {

					};
					fields.put(name, df);
					return df;
				}
			};
			dataTypes.put(name, t);
			return t;
		}

		public void commit() throws OutdatedObjectException {
		}

		@Override
		public DataType getDataType(String typeName) {
			return dataTypes.get(typeName);
		}
	}

	public static ObjectContainer newInstance() {
		return new ObjectContainer() {

			@Override
			public void runTransaction(TransactionalRunnable runner) {
				TransactionImpl t = new TransactionImpl();
				while (true) {
					try {
						runner.run(t);
						t.commit();
						break;
					} catch (OutdatedObjectException e) {
						System.out.println("Object outdated: " + e + ". Rerunning transaction.");
					}
				}
			}
		};
	}

}
