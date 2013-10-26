package se.jsa.jles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.PerformanceTest;
import se.jsa.jles.internal.file.CachingEntryFile;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.testevents.Name;
import se.jsa.jles.internal.testevents.NonSerializableEvent;

@RunWith(value = Parameterized.class)
public class EventStoreTest {

	public static class EmptyEvent {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof EmptyEvent;
		}
		@Override
		public int hashCode() {
			return 1;
		}
		@Override
		public String toString() {
			return "EmptyEvent";
		}
	}

	public static class EmptyEvent2 {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof EmptyEvent3;
		}
		@Override
		public int hashCode() {
			return 1;
		}
		@Override
		public String toString() {
			return "EmptyEvent2";
		}
	}

	public static class EmptyEvent3 {
		@Override
		public boolean equals(Object obj) {
			return obj instanceof EmptyEvent3;
		}
		@Override
		public int hashCode() {
			return 1;
		}
		@Override
		public String toString() {
			return "EmptyEvent3";
		}
	}

	public static class TestEvent {
		private String name;
		private long id;
		private boolean first;

		public TestEvent() {
		}

		public TestEvent(String name, long id, boolean first) {
			this.name = name;
			this.id = id;
			this.first = first;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public boolean getFirst() {
			return first;
		}

		public void setFirst(boolean first) {
			this.first = first;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof TestEvent) {
				TestEvent other = (TestEvent) obj;
				return name.equals(other.name)
						&& id == other.id
						&& first == other.first;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (int) ((name.hashCode() * 31 + id) * 31 + (first ? 1 : 0));
		}

		@Override
		public String toString() {
			return "TestEvent [name=" + name + ", id=" + id + ", first="
					+ first + "]";
		}
	}

	private interface EntryFileFactory {
		public EntryFile create(String fileName);
	}

	@Parameters
	public static Collection<Object[]> entryFiles() {
		return Arrays.asList(
//				new Object[] {
//						new EntryFileFactory() {
//							@Override
//							public EntryFile create(String fileName) {
//								return new SynchronousEntryFile(fileName);
//							}
//							@Override
//							public String toString() { return "SynchronousEntryFile"; }
//						}
//					},
				new Object[] {
						new EntryFileFactory() {
							@Override
							public EntryFile create(String fileName) {
								return new FlippingEntryFile(fileName, new StreamBasedChannelFactory());
							}
							@Override
							public String toString() { return "FlippingEntryFile"; }
						}
					},
				new Object[] {
						new EntryFileFactory() {
							@Override
							public EntryFile create(String fileName) {
								return new CachingEntryFile(new FlippingEntryFile(fileName, new StreamBasedChannelFactory()));
							}
							@Override
							public String toString() { return "FlippingEntryFile+Cache"; }
						}
					}
		);
	}

	private final EventFile eventFile;;
	private final EventStore eventStore;
	private final EntryFile eventEntryFile;
	private final EntryFile indexEntryFile;
	private final EntryFileFactory entryFileFactory;

	public EventStoreTest(EntryFileFactory entryFileFactory) {
		this.entryFileFactory = entryFileFactory;
		this.eventEntryFile = entryFileFactory.create("events.ef");
		this.indexEntryFile = entryFileFactory.create("eventIndexes.if");
		this.eventFile = new EventFile(eventEntryFile);
		this.eventStore = new EventStore(eventFile, indexEntryFile);
	}

	@After
	public void cleanup() {
		eventStore.stop();
		delete("events.ef");
		delete("eventIndexes.if");
		delete("events.def");
		delete("events.if");
	}

	private boolean delete(String fileName) {
		File file = new File(fileName);
		int count = 0;
		while (file.exists() && !file.delete() && count++ < 10) {
			System.out.println("Failed to delete file: " + fileName + " retrying...");
		}
		if (file.exists()) {
			System.out.println("Failed to delete file: " + fileName);
		}
		return true;
	}

	@Test
	public void emptyListWhenNoPresentEvents() throws Exception {
		assertTrue(eventStore.collectEvents(TestEvent.class).isEmpty());
	}

	@Test
	public void writtenEventsShallBeReadBackInSameOrder() throws Exception {
		List<Object> events = Arrays.asList(
				new TestEvent("Joakim", 1L, true),
				new EmptyEvent(),
				new TestEvent("Leena", 2L, false),
				new EmptyEvent(),
				new EmptyEvent(),
				new TestEvent("Venla", 8L, false),
				new EmptyEvent());
		List<TestEvent> expectedTestEvents = Arrays.asList(new TestEvent("Joakim", 1L, true), new TestEvent("Leena", 2L, false), new TestEvent("Venla", 8L, false));
		List<EmptyEvent> expectedEmptyEvents = Arrays.asList(new EmptyEvent(), new EmptyEvent(), new EmptyEvent(), new EmptyEvent());

		for (Object e : events) {
			eventStore.write(e);
		}

		List<?> readEvents = eventStore.collectEvents(TestEvent.class);
		assertEquals(expectedTestEvents, readEvents);
		readEvents = eventStore.collectEvents(EmptyEvent.class);
		assertEquals(expectedEmptyEvents, readEvents);
		readEvents = eventStore.collectEvents();
		assertEquals(events, readEvents);
	}

	@Test
	public void multipleEventsShallBeReadBackInSameOrder() throws Exception {
		List<Object> events = Arrays.asList(
				new TestEvent("Joakim", 1L, true),
				new EmptyEvent(),
				new EmptyEvent2(),
				new TestEvent("Leena", 2L, false),
				new EmptyEvent(),
				new EmptyEvent2(),
				new EmptyEvent(),
				new TestEvent("Venla", 8L, false),
				new EmptyEvent(),
				new EmptyEvent2());
		List<Object> expectedEvents = Arrays.asList(
				new TestEvent("Joakim", 1L, true),
				new EmptyEvent(),
				new TestEvent("Leena", 2L, false),
				new EmptyEvent(),
				new EmptyEvent(),
				new TestEvent("Venla", 8L, false),
				new EmptyEvent());

		for (Object e : events) {
			eventStore.write(e);
		}

		List<?> readEvents = eventStore.collectEvents(TestEvent.class, EmptyEvent.class);
		assertEquals(expectedEvents, readEvents);
	}

	@Test
	public void serializableEventObjectsCanBeResolvedFromNonSerializableEvent() throws Exception {
		NonSerializableEvent nonSerializableEvent = new NonSerializableEvent(Name.valueOf("apa"), new Date());
		eventStore.write(nonSerializableEvent);
		List<Object> events = eventStore.collectEvents(NonSerializableEvent.class);

		Object event = events.get(0);
		assertTrue("Event type was: " + event.getClass(), event instanceof NonSerializableEvent);
		assertTrue(((NonSerializableEvent) event).getName().equals(nonSerializableEvent.getName()));
	}

	@Test
	public void canReadEventFromDifferentSerializableVersions() throws Exception {
		NonSerializableEvent.SerializableEventV1 e1 = new NonSerializableEvent.SerializableEventV1(new NonSerializableEvent(Name.valueOf("1"), new Date(2L)));
		NonSerializableEvent.SerializableEventV2 e2 = new NonSerializableEvent.SerializableEventV2(new NonSerializableEvent(Name.valueOf("2"), new Date(2L)));
		eventStore.write(e1);
		eventStore.write(new EmptyEvent());
		eventStore.write(e2);

		List<Object> events = eventStore.collectEvents(NonSerializableEvent.class);
		assertEquals(2, events.size());
		assertTrue(events.get(0) instanceof NonSerializableEvent);
		assertTrue(events.get(1) instanceof NonSerializableEvent);
		NonSerializableEvent nse1 = (NonSerializableEvent) events.get(0);
		NonSerializableEvent nse2 = (NonSerializableEvent) events.get(1);
		assertEquals("1", 	nse1.getName().toString());
		assertEquals(0, 	nse1.getDate().getTime());
		assertEquals("2", 	nse2.getName().toString());
		assertEquals(2, 	nse2.getDate().getTime());
	}

	@Test
	@Ignore
	public void writeReadPerformance() throws Exception {
		System.out.println("\n\nwriteReadPerformance FilestorageType: " + entryFileFactory);
		final int COUNT = 30000;
		List<Object> events = PerformanceTest.createISEvent(COUNT);

		long start = System.nanoTime();
		for (Object e : events) {
			eventStore.write(e);
		}
		long end = System.nanoTime();
		System.out.println("Wrote " + COUNT + " IntegerStringEvent:s in " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms. " +
				"\nEvents/s = " + (int)((double)COUNT / (end-start) * 1000000000.0));

		start = System.nanoTime();
		eventStore.collectEvents();
		end = System.nanoTime();

		System.out.println("Read " + COUNT + " IntegerStringEvent:s in " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms. " +
				"\nEvents/s = " + (int)((double)COUNT / (end-start) * 1000000000.0));
	}

	@Ignore
	@Test
	public void scenarioPerformance() throws Exception {
		System.out.println("\n\nscenarioPerformance FilestorageType: "+ entryFileFactory);
		final int COUNT = 30000;
		List<Object> events = PerformanceTest.createISEvent(COUNT);

		long start = System.nanoTime();
		for (Object e : events.subList(0, 500)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		for (Object e : events.subList(500, 1000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		for (Object e : events.subList(1000, 10000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		eventStore.collectEvents();
		for (Object e : events.subList(10000, 12000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		for (Object e : events.subList(12000, 13000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		for (Object e : events.subList(13000, 14000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		for (Object e : events.subList(14000, 15000)) {
			eventStore.write(e);
		}
		for (Object e : events.subList(15000, 25000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();
		for (Object e : events.subList(25000, 30000)) {
			eventStore.write(e);
		}
		eventStore.collectEvents();

		long end = System.nanoTime();

		System.out.println("Scenario run time: " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms.");
	}

}
