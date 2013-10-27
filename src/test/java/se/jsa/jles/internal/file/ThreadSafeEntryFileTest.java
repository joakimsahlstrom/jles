package se.jsa.jles.internal.file;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;



public class ThreadSafeEntryFileTest {

	final ThreadSafeEntryFile entryFile = new ThreadSafeEntryFile(new FlippingEntryFile("test.ef", new StreamBasedChannelFactory()));

	@After
	public void teardown() {
		entryFile.close();
		new File("test.ef").delete();
	}

	@Test
	public void normalOperationsWorksAsBefore() throws Exception {
		ByteBuffer buffer = createBuffer();
		entryFile.append(buffer);

		ByteBuffer readEntry = entryFile.readEntry(0);
		assertEntry(readEntry);
	}

	@Test
	public void canMultipleSequentialWritesAndReads() throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		final AtomicInteger counter = new AtomicInteger(0);
		int NUM_ENTRIES = 1000;

		for (int i = 0; i < NUM_ENTRIES; i++) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					ByteBuffer buffer = createBuffer();
					entryFile.append(buffer);
					counter.incrementAndGet();
				}
			});
		}
		while (counter.intValue() < NUM_ENTRIES) { /*busy wait*/ }

		for (int i = 0; i < NUM_ENTRIES; i++) {
			final int entryPosition = i * 13;
			pool.submit(new Runnable() {
				@Override
				public void run() {
					ByteBuffer readEntry = entryFile.readEntry(entryPosition);
					assertEntry(readEntry);
					counter.decrementAndGet();
				}
			});
		}
		while (counter.intValue() > 0) { /*busy wait*/ }
	}

	ByteBuffer createBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + 1);
		buffer.putLong(0);
		buffer.putInt(1);
		buffer.put((byte) 37);
		return buffer;
	}

	void assertEntry(ByteBuffer readEntry) {
		assertEquals(0, readEntry.getLong());
		assertEquals(1, readEntry.getInt());
		assertEquals((byte)37, readEntry.get());
	}

}
