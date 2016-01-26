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
package se.jsa.jles;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.indexing.events.EventIndex;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex;
import se.jsa.jles.internal.indexing.fields.SimpleEventFieldIndex;
import se.jsa.jles.internal.testevents.TestEvent;


public class EventStoreConfigurerTest {

	@Test
	public void fillsEventIndexesIfEventsMissing() throws Exception {
		InMemoryFileRepository inMemoryFileRepository = new InMemoryFileRepository();
		EventIndex eventIndex = new EventIndex(inMemoryFileRepository.getEntryFile(new EntryFileNameGenerator().getEventIndexFileName(new EventTypeId(0L))), new EventTypeId(0L));

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
				new EventTypeId(0L),
				new EventFieldFactory().createEventField(TestEvent.class, "Id", Long.class),
				inMemoryFileRepository.getEntryFile(new EntryFileNameGenerator().getEventFieldIndexFileName(new EventTypeId(0L), "Id")));

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
