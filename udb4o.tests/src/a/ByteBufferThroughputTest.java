package a;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.*;

public class ByteBufferThroughputTest {

	private static final int COUNT = 128 * 1024;

	private static int FACTOR = 4;
	private static final int THREADS = 16/FACTOR;
	protected static final int LONGS_PER_PAGE = 128 * 4;
	protected static final int PAGES_PER_THREAD = 1000*FACTOR;

//	@Test
	public void basic() throws Exception {

		File file = new File("test.dat");
		file.delete();
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileChannel c = raf.getChannel();

		ByteBuffer bb = ByteBuffer.allocate(8 * COUNT);
		LongBuffer fb = bb.asLongBuffer();
		for (int i = 0; i < COUNT; i++) {
			fb.put(i);
		}
		fb.flip();
		int ret = 0;
		while ((ret += c.write(bb, ret)) < COUNT * 8)
			;
		assertEquals(COUNT * 8, ret);

		raf.getFD().sync();
		c.close();

		raf = new RandomAccessFile(file, "rw");
		c = raf.getChannel();
		bb = ByteBuffer.allocate(8 * COUNT);
		fb = bb.asLongBuffer();
		ret = 0;
		while ((ret += c.read(bb, ret)) < COUNT * 8)
			;
		assertEquals(COUNT * 8, ret);
		for (int i = 0; i < COUNT; i++) {
			assertEquals(i, fb.get());
		}

		c.close();
		file.delete();

	}

	@Test
	public void concurrentAppend() throws Exception {

		final File file = new File("test.dat");
		file.delete();

		final CyclicBarrier barrier = new CyclicBarrier(THREADS);
		final AtomicLong nextOffset = new AtomicLong(0);
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < THREADS; i++) {
			final int threadId = i;
			Thread t = new Thread() {
				ByteBuffer bb = ByteBuffer.allocate(LONGS_PER_PAGE*8);
				LongBuffer lb = bb.asLongBuffer();
				RandomAccessFile raf;
				FileChannel fc;
				private long threadCounter = 0;
				@Override
				public void run() {
					try {
						loop(barrier);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				protected void loop(final CyclicBarrier barrier) throws Exception {
					barrier.await();
					
					
					raf = new RandomAccessFile(file, "rw");
					fc = raf.getChannel();
					
					for (int i=0;i<PAGES_PER_THREAD;i++) {
						long offset = nextOffset.getAndAdd(bb.capacity());
						
						populatePage(offset);
						writePage(offset);
						
						raf.getFD().sync();
					}
					
					fc.close();
				}

				protected void populatePage(long offset) throws IOException {
					lb.clear();
					lb.put(offset);
					lb.put(threadId);
					for (int i=0;i<LONGS_PER_PAGE-2;i++) {
						lb.put(threadCounter++);
					}
				}

				protected void writePage(long offset) throws IOException {
					bb.clear();
					bb.position(0);
					bb.limit(bb.capacity());
					long ret = 0;
					while ((ret += fc.write(bb, offset+ret)) < bb.limit());
				}
			};
			t.setDaemon(true);
			threads.add(t);
			t.start();
		}
		
		for(Thread t : threads) {
			t.join();
		}
		
//		RandomAccessFile raf = new RandomAccessFile(file, "r");
//		FileChannel fc = raf.getChannel();
//		
//		ByteBuffer bb = ByteBuffer.allocate(LONGS_PER_PAGE*8);
//		LongBuffer lb = bb.asLongBuffer();
//
//		long[] counters = new long[THREADS];
//		while(fc.read(bb) != -1) {
//			bb.flip();
//			lb.rewind();
//			assertEquals(fc.position()-bb.capacity(), lb.get());
//			int threadId = (int) lb.get();
//			long counter = counters[threadId];
//			for (int i=0;i<LONGS_PER_PAGE-2;i++) {
//				assertEquals(counter++, lb.get());
//			}
//			counters[threadId] = counter;
//		}
//		
//		
//		fc.close();
		


	}
	
}
