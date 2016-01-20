package se.jsa.jles;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import se.jsa.jles.configuration.EntryFileNameGenerator;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.SimpleEventFieldIndex;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.testevents.TestEvent;


public class EventStoreConfigurerTest {

	@Test
	public void fillsEventIndexesIfEventsMissing() throws Exception {
		InMemoryFileRepository inMemoryFileRepository = new InMemoryFileRepository();
		EventIndex eventIndex = new EventIndex(inMemoryFileRepository.getEntryFile(new EntryFileNameGenerator().getEventIndexFileName(0L)), 0L);

		EventStore initialEventStore = EventStoreConfigurer.createMemoryOnlyConfigurer(inMemoryFileRepository)
			.addIndexing(TestEvent.class)
			.configure();
		initialEventStore.write(new TestEvent("1", 1, true));
		initialEventStore.stop();
		// Initial event causes indexing
		assertEquals(1, count(eventIndex.readIndicies().iterator()));


		EventStore nonIndexingEventStore = EventStoreConfigurer.createMemoryOnlyConfigurer(inMemoryFileRepository)
				.configure();
		nonIndexingEventStore.write(new TestEvent("2", 2, false));
		nonIndexingEventStore.stop();
		// No more indexing happens since new event store does not use index for given event type
		assertEquals(1, count(eventIndex.readIndicies().iterator()));


		EventStore indexingEventStore = EventStoreConfigurer.createMemoryOnlyConfigurer(inMemoryFileRepository)
				.addIndexing(TestEvent.class)
				.configure();
		// Index is updated after event store is created
		assertEquals(2, count(eventIndex.readIndicies().iterator()));
		indexingEventStore.stop();
	}

	@Test
	public void fillsEventFieldIndecesIfEventsMissing() throws Exception {
		InMemoryFileRepository inMemoryFileRepository = new InMemoryFileRepository();
		EventFieldIndex eventFieldIndex = new SimpleEventFieldIndex(
				0L,
				new EventFieldFactory().createEventField(Long.class, "Id", TestEvent.class),
				inMemoryFileRepository.getEntryFile(new EntryFileNameGenerator().getEventFieldIndexFileName(0L, "Id")));

		EventStore initialEventStore = EventStoreConfigurer.createMemoryOnlyConfigurer(inMemoryFileRepository)
			.addIndexing(TestEvent.class, "Id")
			.configure();
		initialEventStore.write(new TestEvent("1", 1, true));
		initialEventStore.stop();
		// Initial event causes indexing
		assertEquals(1, count(eventFieldIndex.getIterable(FieldConstraint.noConstraint()).iterator()));


		EventStore nonIndexingEventStore = EventStoreConfigurer.createMemoryOnlyConfigurer(inMemoryFileRepository)
				.configure();
		nonIndexingEventStore.write(new TestEvent("2", 2, false));
		nonIndexingEventStore.stop();
		// No more indexing happens since new event store does not use index for given event type
		assertEquals(1, count(eventFieldIndex.getIterable(FieldConstraint.noConstraint()).iterator()));


		EventStore indexingEventStore = EventStoreConfigurer.createMemoryOnlyConfigurer(inMemoryFileRepository)
				.addIndexing(TestEvent.class, "Id")
				.configure();
		// Index is updated after event store is created
		assertEquals(2, count(eventFieldIndex.getIterable(FieldConstraint.noConstraint()).iterator()));
		indexingEventStore.stop();
	}

	private int count(Iterator<?> it) {
		int count = 0;
		while (it.hasNext()) {
			count++;
			it.next();
		}
		return count;
	}

}
