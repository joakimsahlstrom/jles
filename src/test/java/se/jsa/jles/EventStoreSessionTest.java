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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.EventStoreConfigurer.IndexType;
import se.jsa.jles.EventStoreTest.EmptyEvent;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.testevents.Name;
import se.jsa.jles.internal.testevents.NonSerializableEvent;

public class EventStoreSessionTest {

	private final StreamBasedChannelFactory fileChannelFactory = new StreamBasedChannelFactory();
	private final EventStoreConfigurer configurer = EventStoreConfigurer.createFileBasedConfigurer(fileChannelFactory).indexing(IndexType.SINGLE_FILE);
	private EventStore eventStore = configurer.configure();

	@After
	public void teardown() {
		eventStore.stop();
		for (String fileName : configurer.getFiles()) {
			delete(fileName);
		}
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
	public void canReadEventFromDifferentSerializableVersions() throws Exception {
		NonSerializableEvent.SerializableEventV1 e1 = new NonSerializableEvent.SerializableEventV1(new NonSerializableEvent(Name.valueOf("1"), new Date(2L)));
		NonSerializableEvent.SerializableEventV2 e2 = new NonSerializableEvent.SerializableEventV2(new NonSerializableEvent(Name.valueOf("2"), new Date(2L)));
		eventStore.write(e1);
		eventStore.write(new EmptyEvent());
		eventStore.write(e2);

		eventStore.stop();
		eventStore = configurer.configure();

		Iterator<Object> events = eventStore.readEvents(EventQuery.select(NonSerializableEvent.class)).iterator();
		NonSerializableEvent nse1 = (NonSerializableEvent) events.next();
		NonSerializableEvent nse2 = (NonSerializableEvent) events.next();
		assertFalse(events.hasNext());
		assertEquals("1", 	nse1.getName().toString());
		assertEquals(0, 	nse1.getDate().getTime());
		assertEquals("2", 	nse2.getName().toString());
		assertEquals(2, 	nse2.getDate().getTime());

		assertEquals(3, size(eventStore.readEvents(EventQuery.select(NonSerializableEvent.class).join(EmptyEvent.class)).iterator()));
	}

	private int size(Iterator<Object> readEvents) {
		int count = 0;
		while (readEvents.hasNext()) { readEvents.next(); count++; }
		return count;
	}

}
