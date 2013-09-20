package se.jsa.jles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.jsa.jles.EventStoreTest.EmptyEvent;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.testevents.Name;
import se.jsa.jles.internal.testevents.NonSerializableEvent;

public class EventStoreSessionTest {

	@Before
	@After
	public void teardown() {
		delete("events.if");
		delete("events.ef");
		delete("events.def");
	}

	private void delete(String fileName) {
		new File(fileName).delete();
	}

	@Test
	public void canReadEventFromDifferentSerializableVersions() throws Exception {
		EventStore eventStore = buildEventStore();
		eventStore.init();

		NonSerializableEvent.SerializableEventV1 e1 = new NonSerializableEvent.SerializableEventV1(new NonSerializableEvent(Name.valueOf("1"), new Date(2L)));
		NonSerializableEvent.SerializableEventV2 e2 = new NonSerializableEvent.SerializableEventV2(new NonSerializableEvent(Name.valueOf("2"), new Date(2L)));
		eventStore.write(e1);
		eventStore.write(new EmptyEvent());
		eventStore.write(e2);

		eventStore = buildEventStore();
		eventStore.init();

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

		assertEquals(3, eventStore.collectEvents().size());
	}

	private EventStore buildEventStore() {
		return EventStore.create(new StreamBasedChannelFactory());
	}

}
