//package com.udb4o.test;
//
//import java.io.*;
//import java.util.*;
//
//public class SimpleDataContainer implements DataContainer {
//	
//
//	public final class DataImpl implements Data, AsymmetricExternalizable {
//		
//		private static final byte SERIAL_VERSION = 1;
//		
//		private long createdTimestamp = System.currentTimeMillis();
//		private long modifiedTimestamp = createdTimestamp;
//		private final UUID key;
//		
//		private boolean[] modified;
//		private Object[] values;
//		
//		private boolean dirty = false;
//
//		public DataImpl(UUID key) {
//			this.key = key;
//		}
//
//		@Override
//		public void set(int field, Object value) {
//			touch();
//			ensureCapacity(field);
//			modified[field] = true;
//			values[field] = value;
//		}
//
//		private void ensureCapacity(int field) {
//			if (modified != null && modified.length > field) {
//				return;
//			}
//			boolean[] m = new boolean[field+1];
//			if (modified != null) System.arraycopy(modified, 0, m, 0, modified.length);
//			modified = m;
//			Object[] o = new Object[field+1];
//			if (values != null) System.arraycopy(values, 0, o, 0, values.length);
//			values = o;
//		}
//
//		private void touch() {
//			if (!dirty) {
//				dirtyDatas.add(this);
//			}
//			dirty = true;
//			modifiedTimestamp = System.currentTimeMillis();
//		}
//
//		@Override
//		public Object get(int field) {
//			return values[field];
//		}
//
//		public void writeMetaInfo(AsymmetricDataOutput out) throws IOException {
//			out.put("version", SERIAL_VERSION);
//			out.putTimestamp("createdTimestamp", createdTimestamp);
//			out.putTimestamp("modifiedTimestamp", modifiedTimestamp);
//		}
//
//		@Override
//		public void writeExternal(AsymmetricDataOutput out) throws IOException {
//			writeMetaInfo(out);
//		}
//
//		@Override
//		public void readExternal(AsymmetricDataInput in) throws IOException {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void writeChanges(SimpleAsymmetricDataOutput out) throws IOException {
//			for(int i=0;i<modified.length;i++) {
//				if (!modified[i]) continue;
//				out.put(null, null);
//			}
//		}
//
//		public UUID getKey() {
//			return key;
//		}
//	}
//
//	private final Storage storage;
//	public Collection<DataImpl> dirtyDatas = new ArrayList<SimpleDataContainer.DataImpl>();
//
//	public SimpleDataContainer(Storage storage) {
//		this.storage = storage;
//	}
//
//	@Override
//	public Data createData(UUID key) {
//		DataImpl data = new DataImpl(key);
//		SimpleAsymmetricDataOutput out = new SimpleAsymmetricDataOutput();
//		try {
//			data.writeMetaInfo(out);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		storage.put(key, out.toByteBuffer());
//		return data;
//	}
//
//	@Override
//	public Data getData(UUID key) {
//		return null;
//	}
//
//	@Override
//	public void dispose() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void flush() {
//		SimpleAsymmetricDataOutput out = new SimpleAsymmetricDataOutput();
//		for (DataImpl d : dirtyDatas) {
//			out.resetBuffer();
//			try {
//				d.writeChanges(out);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			storage.putMore(d.getKey(), out.toByteBuffer());
//
//		}
//		storage.flush();
//	}
//
//
//}
