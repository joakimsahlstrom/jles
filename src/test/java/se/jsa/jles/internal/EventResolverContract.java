package se.jsa.jles.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.jsa.jles.internal.eventdefinitions.EventResolver;
import se.jsa.jles.internal.testevents.TestEvent;

public abstract class EventResolverContract {

	protected abstract EventResolver getEventResolver();

	@Test
	public void ifAnEventIsAlreadySerializableItIsReturnedAsIsFromAllMethods() throws Exception {
		TestEvent testEvent = new TestEvent("n", 1L, true);
		assertEquals(testEvent, getEventResolver().getSerializableEvent(testEvent));
		assertEquals(TestEvent.class, getEventResolver().getSerializableEventTypes(new Class[] {TestEvent.class})[0]);
	}

}
