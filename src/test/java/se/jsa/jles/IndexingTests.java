package se.jsa.jles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.EventStoreTest.EmptyEvent;
import se.jsa.jles.EventStoreTest.EmptyEvent2;
import se.jsa.jles.EventStoreTest.EmptyEvent3;
import se.jsa.jles.EventStoreTest.TestEvent;
import se.jsa.jles.internal.EventFieldConstraint;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;

public class IndexingTests {

	private final EventStoreConfigurer configurer = new EventStoreConfigurer(new StreamBasedChannelFactory())
		.addIndexing(EmptyEvent3.class);
	private final EventStore es = configurer.configure();

	@Test
	public void canSupplyConstraint() throws Exception {
		es.write(new TestEvent("a", 0, true));
		es.write(new TestEvent("a", 1, true));
		es.write(new TestEvent("a", 2, true));
		List<Object> events = EventStore.collect(
			es.readEvents(TestEvent.class, new Match() {
				@Override
				public Iterable<EventId> buildFilteringIterator(TypedEventRepo eventRepo) {
					return eventRepo.getIterator(EventFieldConstraint.create("Id", new FieldConstraint<Long>() {
						@Override
						public boolean isSatisfied(Long eventFieldValue) {
							boolean res = Long.valueOf(1).equals(eventFieldValue);
							return res;
						}
						@Override
						public Class<Long> getFieldType() {
							return Long.class;
						}
					}));
				}
			})
		);
		assertEquals(Arrays.asList(new TestEvent("a", 1, true)), events);
	}

	@Test
	public void indexCanBeAddedAtLaterStartup() throws Exception {
		es.write(new TestEvent("a", 0, true));
		es.write(new TestEvent("a", 1, true));
		es.write(new TestEvent("a", 2, true));
		es.stop();

		EventStore es = configurer
				.addIndexing(TestEvent.class)
				.configure();
		Iterator<Object> events = es.readEvents(TestEvent.class).iterator();
		assertEquals(0L, ((TestEvent)events.next()).getId());
		assertEquals(1L, ((TestEvent)events.next()).getId());
		assertEquals(2L, ((TestEvent)events.next()).getId());
	}

	@Test
	public void indexPerformanceTest() throws Exception {
		List<Object> events = createEEvents(5000, 0.01d);

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
	}

	@After
	public void teardown() {
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
		for (int i = 0; i < num; i++) {
			if (random.nextDouble() < d) {
				result.add(new EmptyEvent2());
				result.add(new EmptyEvent3());
			} else {
				result.add(new EmptyEvent());
			}
		}
		return result;
	}

}
