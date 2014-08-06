//package com.udb4o.btree;
//
//import java.io.*;
//import java.nio.*;
//import java.nio.channels.*;
//import java.util.*;
//import java.util.Map.Entry;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.*;
//
//public class LongBTree {
//	
//	public final static int PAGE_LENGTH = 1024;
//	public final static int INDEX_PER_PAGE = (PAGE_LENGTH-8)/16;
//	
//	public long currentRootOffset = 0;
//
//	
//	private final class TransactionImpl implements Transaction {
//		
//		
//		private FileChannel buffer;
//		private Map<Long, Long> map = new TreeMap<Long, Long>();
//		private long rootOffset = -1;
//		private Map<Long, Page> pagesCache = new HashMap<Long, Page>();
//		
//		{
//			try {
//				buffer = new RandomAccessFile(storage, "rw").getChannel();
//			} catch (FileNotFoundException e) {
//				throw new RuntimeException(e);
//			}
//
//		}
//		
//		private final class Page {
//			long offset;
//			FloatBuffer buffer;
//			long position = 0;
//			public Page(long offset) {
//				this.offset = offset;
//			}
//			public void read(FileChannel in) {
//				in.position(offset);
////				in.transferTo(position, count, target)
//			}
//		}
//		
//		@Override
//		public void put(long key, byte[] value) {
//			if (rootOffset == -1) {
//				init();
//			}
//			
//			long offset = fileSize.getAndAdd(value.length);
//			try {
//				buffer.position(offset);
//				buffer.write(value);
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			
//			map.put(key, offset);
//			
//		}
//
//		private void init() {
//			rootOffset = currentRootOffset();
//		} 
//		
//		private Page page(long offset) {
//			Page page = pagesCache.get(offset);
//			if (page != null) {
//				return page;
//			}
//			
//			page = new Page(offset);
//			page.read(buffer);
//			pagesCache.put(offset, page);
//			
//			return page;
//		}
//
//		@Override
//		public void commit() {
//			
//			for (Entry<Long, Long> entry : map.entrySet()) {
//				long key = entry.getKey();
//				long value = entry.getValue();
//				
//				try {
//					
//					long slot = 0;
//					
//					Deque<byte[]> pages = new LinkedList<byte[]>();
//
//					long pageOffset = rootOffset;
//					start: while (pageOffset == rootOffset || pageOffset != 0) {
//						byte[] page = new byte[PAGE_LENGTH];
//						pages.push(page);
//						buffer.position(pageOffset);
//						buffer.readFully(page);
//						
//						int offset = 8;
//						while (offset < PAGE_LENGTH) {
//							long read = readLong(page, offset);
//							if (key < read) {
//								pageOffset = readLong(page, offset-8);
//								if (pageOffset == 0) {
//									
//								}
//								break;
//							}
//							if (key == read) {
//								slot = readLong(page, offset+8);
//								break start;
//							}
//							if (key > read) {
//								pageOffset = readLong(page, offset+8);
//								break;
//							}
//							offset += 16;
//						}
//					}
//					
//					
//					
//				} catch (IOException e1) {
//					throw new RuntimeException(e1);
//				}
//				
//				
//			}
//			
//			dispose();
//		}
//
//		public final long readLong(Page page, int offset) throws IOException {
//			byte[] readBuffer = page.buffer;
//			return (((long) readBuffer[offset++] << 56)
//					+ ((long) (readBuffer[offset++] & 255) << 48)
//					+ ((long) (readBuffer[offset++] & 255) << 40)
//					+ ((long) (readBuffer[offset++] & 255) << 32)
//					+ ((long) (readBuffer[offset++] & 255) << 24)
//					+ ((readBuffer[offset++] & 255) << 16)
//					+ ((readBuffer[offset++] & 255) << 8) + ((readBuffer[offset++] & 255) << 0));
//		}
//
//
//		private byte[] rootPage() throws IOException {
//			buffer.position(rootOffset);
//			int length = buffer.readInt();
//			return null;
//		}
//
//		protected void dispose() {
//			transactionPool.addLast(this);
//		}
//
//		@Override
//		public void rollback() {
//			rootOffset = -1;
//			dispose();
//		}
//
//		@Override
//		public byte[] get(long key) {
//			if (rootOffset == -1) {
//				init();
//			}
//			
//			long pageOffset = rootOffset;
//			start: while (pageOffset == rootOffset || pageOffset != 0) {
//				Page page = page(pageOffset);
//				
//				int offset = 8;
//				while (offset < PAGE_LENGTH) {
//					long read = readLong(page, offset);
//					if (key < read) {
//						pageOffset = readLong(page, offset-8);
//						if (pageOffset == 0) {
//							
//						}
//						break;
//					}
//					if (key == read) {
//						slot = readLong(page, offset+8);
//						break start;
//					}
//					if (key > read) {
//						pageOffset = readLong(page, offset+8);
//						break;
//					}
//					offset += 16;
//				}
//			}
//
//			
//			return null;
//		}
//		
//		private void writeLong(byte[] buf, long value) {
//			buf[0] = (byte)(value >>> 56);
//			buf[1] = (byte)(value >>> 48);
//			buf[2] = (byte)(value >>> 40);
//			buf[3] = (byte)(value >>> 32);
//			buf[4] = (byte)(value >>> 24);
//			buf[5] = (byte)(value >>> 16);
//			buf[6] = (byte)(value >>>  8);
//			buf[7] = (byte)(value >>>  0);
//			
//		}
//
//		
//		
//	}
//
//	private final File storage;
//	private final AtomicLong fileSize = new AtomicLong(0);
//	private BlockingDeque<TransactionImpl> transactionPool = new LinkedBlockingDeque<TransactionImpl>();
//
//	public LongBTree(File storage) {
//		this.storage = storage;
//	}
//
//	public long currentRootOffset() {
//		return currentRootOffset;
//	}
//
//	public Transaction beginTransaction() {
//		TransactionImpl t = transactionPool.poll();
//		if (t != null) {
//			return t;
//		}
//		return new TransactionImpl();
//	}
//	
//}