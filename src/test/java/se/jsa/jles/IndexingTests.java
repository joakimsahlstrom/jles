package se.jsa.jles;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import se.jsa.jles.EventStoreTest.EmptyEvent;
import se.jsa.jles.EventStoreTest.EmptyEvent2;
import se.jsa.jles.EventStoreTest.EmptyEvent3;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;

public class IndexingTests {

	@Test
	public void indexPerformanceTest() throws Exception {
		List<Object> events = createEEvents(3000, 0.01d);

		EventStoreConfigurer configurer = new EventStoreConfigurer(new StreamBasedChannelFactory())
			.addIndexing(EmptyEvent3.class)
			.testableEventDefinitions();
		EventStore es = configurer.configure();

		for (Object event : events) {
			es.write(event);
		}

		long start2 = System.nanoTime();
		es.collectEvents(EmptyEvent2.class);
		long end2 = System.nanoTime();

		long start3 = System.nanoTime();
		es.collectEvents(EmptyEvent3.class);
		long end3 = System.nanoTime();

		long unindexedRead = end2 - start2;
		long indexedRead = end3 - start3;
		assertTrue("Indexed read should be at least a factor 10 faster under conditions given in this test case (" + indexedRead + " vs " + unindexedRead + ")", indexedRead * 10 < unindexedRead);
		es.stop();

		for (String fileName : configurer.getFiles()) {
			delete(fileName);
		}
	}

	private boolean delete(String fileName) {
		File file = new File(fileName);
		int count = 0;
		while (file.exists() && !file.delete() && count++ < 10) {
			System.out.println("Failed to delete file: " + fileName);
		}
		return true;
	}

	private static Random random = new Random(System.nanoTime());
	public static List<Object> createEEvents(int num, double d) {
		List<Object> result = new ArrayList<Object>(num);
		int hits = 0;
		for (int i = 0; i < num; i++) {
			if (random.nextDouble() < d) {
				result.add(new EmptyEvent2());
				result.add(new EmptyEvent3());
				hits++;
			} else {
				result.add(new EmptyEvent());
			}
		}
		System.out.println("#Hits: " + hits);
		return result;
	}

}
