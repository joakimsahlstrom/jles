package se.jsa.jles.internal;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.SynchronousEntryFile;
import se.jsa.jles.internal.testevents.TestEvent;
import se.jsa.jles.internal.util.Objects;

public class IndexingTest {

	@After
	public void setup() {
		delete("test.if");
		delete("field.if");
	}

	private boolean delete(String fileName) {
		File file = new File(fileName);
		int count = 0;
		while (file.exists() && !file.delete() && count++ < 10) {
			System.out.println("Failed to delete file: " + fileName);
		}
		return true;
	}

	private final SynchronousEntryFile eventTypeIndexFile = new SynchronousEntryFile("test.if");
	private final SynchronousEntryFile fieldIndexFile = new SynchronousEntryFile("field.if");
	private final MemoryBasedEventDefinitions eventDefinitions = new MemoryBasedEventDefinitions();
	private final Set<Long> eventTypeIds = eventDefinitions.getEventTypeIds(TestEvent.class);
	private final Long eventTypeId = eventTypeIds.iterator().next();

	private class EqualsLongConstraint extends FieldConstraint<Long> {
		private final Long value;

		public EqualsLongConstraint(Long value) {
			this.value = Objects.requireNonNull(value);
		}
		@Override
		protected boolean isSatisfied(Long eventFieldValue) {
			return this.value.equals(eventFieldValue);
		}

		@Override
		protected Class<Long> getFieldType() {
			return Long.class;
		}
	}

	@Test
	public void canIndexFields() throws Exception {
		SimpleEventFieldIndex eventFieldIndex = new SimpleEventFieldIndex(eventTypeId, eventDefinitions.getEventField(eventTypeId, "Id"), fieldIndexFile);
		Indexing indexing = createIndexing(eventFieldIndex);

		for (TestEvent te : Arrays.asList(new TestEvent("a", 0, true), new TestEvent("b", 1, true), new TestEvent("a", 0, true))) {
			indexing.onNewEvent(0, eventDefinitions.getEventSerializer(te), te);
		}

		assertContainsEvents(eventFieldIndex.getIterable(EventFieldConstraint.create("Id", new EqualsLongConstraint(0L))), 0L, 2L);
	}

	@Test
	public void indexPersistThroughRestart() throws Exception {
		SimpleEventFieldIndex eventFieldIndex = new SimpleEventFieldIndex(eventTypeId, eventDefinitions.getEventField(eventTypeId, "Id"), fieldIndexFile);
		Indexing indexing = createIndexing(eventFieldIndex);

		for (TestEvent te : Arrays.asList(new TestEvent("a", 0, true), new TestEvent("b", 1, true), new TestEvent("a", 0, true))) {
			indexing.onNewEvent(0, eventDefinitions.getEventSerializer(te), te);
		}

		// recreate event field index
		eventFieldIndex = new SimpleEventFieldIndex(eventTypeId, eventDefinitions.getEventField(eventTypeId, "Id"), fieldIndexFile);

		assertContainsEvents(eventFieldIndex.getIterable(EventFieldConstraint.create("Id", new EqualsLongConstraint(0L))), 0L, 2L);
	}

	private void assertContainsEvents(Iterable<EventId> iterable, long... ids) {
		Iterator<EventId> indicies = iterable.iterator();
		int paramNum = 0;
		for (long id : ids) {
			assertEquals("Error or param " + paramNum++, id, indicies.next().getEventIdByType());
		}
	}

	private Indexing createIndexing(SimpleEventFieldIndex eventFieldIndex) {
		return new Indexing(new IndexFile(new StorableLongField(), eventTypeIndexFile),
				Collections.<Long, EventIndex>emptyMap(),
				Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>singletonMap(eventFieldIndex.getFieldId(), eventFieldIndex),
				false);
	}

}
