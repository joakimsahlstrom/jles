package se.jsa.jles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.EventStoreTest.EmptyEvent;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.testevents.Name;
import se.jsa.jles.internal.testevents.NonSerializableEvent;

public class EventStoreSessionTest {

	private final StreamBasedChannelFactory fileChannelFactory = new StreamBasedChannelFactory();
	private final EventStoreConfigurer configurer = EventStoreConfigurer.createFileBasedConfigurer(fileChannelFactory);
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

		List<Object> events = eventStore.collectEvents(EventQuery.builder().query(NonSerializableEvent.class).build());
		assertEquals(2, events.size());
		assertTrue(events.get(0) instanceof NonSerializableEvent);
		assertTrue(events.get(1) instanceof NonSerializableEvent);
		NonSerializableEvent nse1 = (NonSerializableEvent) events.get(0);
		NonSerializableEvent nse2 = (NonSerializableEvent) events.get(1);
		assertEquals("1", 	nse1.getName().toString());
		assertEquals(0, 	nse1.getDate().getTime());
		assertEquals("2", 	nse2.getName().toString());
		assertEquals(2, 	nse2.getDate().getTime());

		assertEquals(3, eventStore.collectEvents(EventQuery.builder().query(NonSerializableEvent.class).query(EmptyEvent.class).build()).size());
	}

}
