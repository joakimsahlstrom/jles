package se.jsa.jles.internal.eventdefinitions;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import se.jsa.jles.internal.EventResolverContract;
import se.jsa.jles.internal.testevents.EventWithNonStaticSerializable;
import se.jsa.jles.internal.testevents.Name;
import se.jsa.jles.internal.testevents.NonSerializableEvent;

public class ConventionbasedEventResolverTest extends EventResolverContract {

	private final ConventionbasedEventResolver resolver = new ConventionbasedEventResolver();

	@Override
	protected EventResolver getEventResolver() {
		return resolver;
	}

	@Test
	public void canConvertNonSerializableEventToSerializable() throws Exception {
		NonSerializableEvent nonSerializableEvent = new NonSerializableEvent(Name.valueOf("apa"), new Date());
		Object serializableEvent = resolver.getSerializableEvent(nonSerializableEvent);
		assertTrue(serializableEvent instanceof NonSerializableEvent.SerializableEventV2);
	}

	@Test
	public void canFindSerializableTypesFromNonSerializableType() throws Exception {
		Class<?>[] serializableEventTypes = resolver.getSerializableEventTypes(new Class[] {NonSerializableEvent.class});
		assertTrue(Arrays.equals(new Class[] {NonSerializableEvent.SerializableEventV2.class}, serializableEventTypes));
	}

	@Test(expected = RuntimeException.class)
	public void doesNotAcceptNonStaticSerializableClass() throws Exception {
		Class<?>[] serializableEventTypes = resolver.getSerializableEventTypes(new Class[] {EventWithNonStaticSerializable.class});
		assertTrue(Arrays.equals(new Class[] {EventWithNonStaticSerializable.class}, serializableEventTypes));
	}

}
