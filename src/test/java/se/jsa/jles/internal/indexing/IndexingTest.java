/*
 * Copyright 2016 Joakim Sahlström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jsa.jles.internal.indexing;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.SynchronousEntryFile;
import se.jsa.jles.internal.indexing.events.EventIndex;
import se.jsa.jles.internal.indexing.events.EventIndexingSingleFile;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex;
import se.jsa.jles.internal.indexing.fields.SimpleEventFieldIndex;
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
	private final Set<EventTypeId> eventTypeIds = eventDefinitions.getEventTypeIds(TestEvent.class);
	private final EventTypeId eventTypeId = eventTypeIds.iterator().next();

	private class EqualsLongConstraint extends Constraint {
		private final Long value;

		public EqualsLongConstraint(Long value) {
			this.value = Objects.requireNonNull(value);
		}
		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return this.value.equals(eventFieldValue);
		}

		@Override
		public Class<Long> getFieldType() {
			return Long.class;
		}
	}

	@Test
	public void canIndexFields() throws Exception {
		SimpleEventFieldIndex eventFieldIndex = new SimpleEventFieldIndex(eventTypeId, eventDefinitions.getEventField(eventTypeId, "Id"), fieldIndexFile);
		Indexing indexing = createIndexing(eventFieldIndex);

		int eventId = 0;
		for (TestEvent te : Arrays.asList(new TestEvent("a", 0, true), new TestEvent("b", 1, true), new TestEvent("a", 0, true))) {
			indexing.onNewEvent(eventId++, eventDefinitions.getEventSerializer(te), te);
		}

		assertContainsEvents(eventFieldIndex.getIterable(FieldConstraint.create("Id", new EqualsLongConstraint(0L))), 0L, 2L);
	}

	@Test
	public void indexPersistThroughRestart() throws Exception {
		SimpleEventFieldIndex eventFieldIndex = new SimpleEventFieldIndex(eventTypeId, eventDefinitions.getEventField(eventTypeId, "Id"), fieldIndexFile);
		Indexing indexing = createIndexing(eventFieldIndex);

		int eventId = 0;
		for (TestEvent te : Arrays.asList(new TestEvent("a", 0, true), new TestEvent("b", 1, true), new TestEvent("a", 0, true))) {
			indexing.onNewEvent(eventId++, eventDefinitions.getEventSerializer(te), te);
		}

		// recreate event field index
		eventFieldIndex = new SimpleEventFieldIndex(eventTypeId, eventDefinitions.getEventField(eventTypeId, "Id"), fieldIndexFile);

		assertContainsEvents(eventFieldIndex.getIterable(FieldConstraint.create("Id", new EqualsLongConstraint(0L))), 0L, 2L);
	}

	private void assertContainsEvents(Iterable<EventId> iterable, long... ids) {
		Iterator<EventId> indicies = iterable.iterator();
		int paramNum = 0;
		for (long id : ids) {
			assertEquals("Error or param " + paramNum++, id, indicies.next().toLong());
		}
	}

	private Indexing createIndexing(SimpleEventFieldIndex eventFieldIndex) {
		return new Indexing(
				new EventIndexingSingleFile(
						new IndexFile(new StorableLongField(), eventTypeIndexFile),
						Collections.<EventTypeId, EventIndex>emptyMap()),
				Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>singletonMap(eventFieldIndex.getFieldId(), eventFieldIndex),
				false);
	}

}
