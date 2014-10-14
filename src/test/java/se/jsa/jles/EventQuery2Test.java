package se.jsa.jles;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import se.jsa.jles.internal.testevents.MyEvent;
import se.jsa.jles.internal.testevents.MyEvent2;

public class EventQuery2Test {

	EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure();
	
	@Test
	public void canQueryForAllEventsOfOneType() throws Exception {
		MyEvent expectedEvent = new MyEvent(1);
		eventStore.write(expectedEvent);
		assertEquals(expectedEvent, eventStore.readEvents(EventQuery2.select(MyEvent.class)).iterator().next());
	}
	
	@Test
	public void canQueryByFieldEqualityForOneEventType() throws Exception {
		MyEvent otherEvent = new MyEvent(1);
		MyEvent expectedEvent = new MyEvent(2);
		eventStore.write(otherEvent);
		eventStore.write(expectedEvent);
		assertEquals(expectedEvent, eventStore.readEvents(EventQuery2.select(MyEvent.class).where("Num").is(2)).iterator().next());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void doesNotAllowQueryForNonExistingField() throws Exception {
		EventQuery2.select(MyEvent.class).where("Num2");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void doesNotAllowQueryWithDifferentTypeForEquality() throws Exception {
		EventQuery2.select(MyEvent.class).where("Num").is(2L);
	}
	
	@Test
	public void canQueryForAllEventsOfMultipleTypes() throws Exception {
		MyEvent expectedEvent1 = new MyEvent(1);
		MyEvent2 expectedEvent2 = new MyEvent2(2);
		eventStore.write(expectedEvent1);
		eventStore.write(expectedEvent2);
		Iterator<Object> iterator = eventStore.readEvents(EventQuery2.select(MyEvent.class).join(MyEvent2.class)).iterator();
		assertEquals(expectedEvent1, iterator.next());
		assertEquals(expectedEvent2, iterator.next());
	}
	
	@Test
	public void canQueryByFieldEqualityForMultipleEventTypes() throws Exception {
		MyEvent expectedEvent1 = new MyEvent(1);
		MyEvent otherEvent1 = new MyEvent(2);
		MyEvent2 otherEvent2 = new MyEvent2(3);
		MyEvent2 expectedEvent2 = new MyEvent2(4);
		eventStore.write(expectedEvent1);
		eventStore.write(otherEvent1);
		eventStore.write(otherEvent2);
		eventStore.write(expectedEvent2);
		Iterator<Object> iterator = eventStore.readEvents(
				EventQuery2
					.select(MyEvent.class).where("Num").is(1)
					.join(MyEvent2.class).where("Num").is(4L)
					).iterator();
		assertEquals(expectedEvent1, iterator.next());
		assertEquals(expectedEvent2, iterator.next());
	}
	
	@Test
	public void canQueryByFieldInequality() throws Exception {
		
	}
	
	@Test
	public void canQueryByNumericFieldGreaterThan() throws Exception {
		
	}
	
	@Test
	public void canQueryByNumericFieldLessThan() throws Exception {
		
	}
	
}
