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

import java.util.Iterator;

import org.junit.Test;

import se.jsa.jles.EventStoreConfigurer.IndexType;
import se.jsa.jles.internal.testevents.MyEvent;
import se.jsa.jles.internal.testevents.MyEvent2;

public class EventQueryTest {

	EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().indexing(IndexType.SINGLE_FILE).configure();

	@Test
	public void canQueryForAllEventsOfOneType() throws Exception {
		MyEvent expectedEvent = new MyEvent(1);
		eventStore.write(expectedEvent);
		assertEquals(expectedEvent, eventStore.readEvents(EventQuery.select(MyEvent.class)).iterator().next());
	}

	@Test
	public void canQueryByFieldEqualityForOneEventType() throws Exception {
		MyEvent otherEvent = new MyEvent(1);
		MyEvent expectedEvent = new MyEvent(2);
		eventStore.write(otherEvent);
		eventStore.write(expectedEvent);
		assertEquals(expectedEvent, eventStore.readEvents(EventQuery.select(MyEvent.class).where("Num").is(2)).iterator().next());
	}

	@Test(expected = IllegalArgumentException.class)
	public void doesNotAllowQueryForNonExistingField() throws Exception {
		EventQuery.select(MyEvent.class).where("Num2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void doesNotAllowQueryWithDifferentTypeForEquality() throws Exception {
		EventQuery.select(MyEvent.class).where("Num").is(2L);
	}

	@Test
	public void canQueryForAllEventsOfMultipleTypes() throws Exception {
		MyEvent expectedEvent1 = new MyEvent(1);
		MyEvent2 expectedEvent2 = new MyEvent2(2);
		eventStore.write(expectedEvent1);
		eventStore.write(expectedEvent2);
		Iterator<Object> iterator = eventStore.readEvents(EventQuery.select(MyEvent.class).join(MyEvent2.class)).iterator();
		assertEquals(expectedEvent1, iterator.next());
		assertEquals(expectedEvent2, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void canQueryByFieldEqualityForMultipleEventTypes() throws Exception {
		MyEvent expectedEvent1 = new MyEvent(1);
		MyEvent otherEvent1 = new MyEvent(2);
		MyEvent2 otherEvent2 = new MyEvent2(3);
		MyEvent2 expectedEvent2 = new MyEvent2(4);
		MyEvent2 expectedEvent3 = new MyEvent2(4);
		eventStore.write(expectedEvent1);
		eventStore.write(otherEvent1);
		eventStore.write(otherEvent2);
		eventStore.write(expectedEvent2);
		eventStore.write(expectedEvent3);
		Iterator<Object> iterator = eventStore.readEvents(
				EventQuery
					.select(MyEvent.class).where("Num").is(1)
					.join(MyEvent2.class).where("Num").is(4L)
					).iterator();
		assertEquals(expectedEvent1, iterator.next());
		assertEquals(expectedEvent2, iterator.next());
		assertEquals(expectedEvent3, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void canQueryByNumericFieldGreaterThan() throws Exception {
		MyEvent otherEvent1 = new MyEvent(1);
		MyEvent expectedEvent1 = new MyEvent(2);
		MyEvent expectedEvent2 = new MyEvent(3);
		eventStore.write(expectedEvent1);
		eventStore.write(otherEvent1);
		eventStore.write(expectedEvent2);
		Iterator<Object> iterator = eventStore.readEvents(
				EventQuery
					.select(MyEvent.class).where("Num").isGreaterThan(1)
					).iterator();
		assertEquals(expectedEvent1, iterator.next());
		assertEquals(expectedEvent2, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void canQueryByNumericFieldLessThan() throws Exception {
		MyEvent expectedEvent1 = new MyEvent(1);
		MyEvent otherEvent1 = new MyEvent(2);
		MyEvent otherEvent2 = new MyEvent(3);
		eventStore.write(expectedEvent1);
		eventStore.write(otherEvent1);
		eventStore.write(otherEvent2);
		Iterator<Object> iterator = eventStore.readEvents(
				EventQuery
					.select(MyEvent.class).where("Num").isLessThan(2)
					).iterator();
		assertEquals(expectedEvent1, iterator.next());
		assertFalse(iterator.hasNext());
	}

}
