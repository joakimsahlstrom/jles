package se.jsa.jles.internal.eventdefinitions;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.EventFileTest;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.file.SynchronousEntryFile;

public class EventDefinitionFileTest {

	private final EventFieldFactory eventFieldFactory = new EventFieldFactory();
	private final EventDefinitionFile edf = new EventDefinitionFile(new SynchronousEntryFile("events.edf"));

	@After
	public void clearTestFiles() {
		edf.close();
		new File("events.edf").delete();
	}

	@Test
	public void writeAndReadSingleEventDefinition() throws Exception {
		EventDefinition eventDefinition = createEventDefinition(EventFileTest.MultipleFieldsEvent.class);
		edf.write(eventDefinition);

		assertEquals(Arrays.asList(eventDefinition), edf.readAllEventDefinitions());
	}


	@Test
	public void writeAndReadMultipleEventDefinitions() throws Exception {
		EventDefinition ed1 = createEventDefinition(EventFileTest.MultipleFieldsEvent.class);
		EventDefinition ed2 = createEventDefinition(EventFileTest.SingleIntegerEvent.class);
		EventDefinition ed3 = createEventDefinition(EventFileTest.SingleCharEvent.class);
		edf.write(ed1);
		edf.write(ed2);
		edf.write(ed3);

		assertEquals(Arrays.asList(ed1, ed2, ed3), edf.readAllEventDefinitions());
	}

	private EventDefinition createEventDefinition(Class<?> eventType) {
		return new EventDefinition((long)eventType.hashCode(), eventType, eventFieldFactory.fromEventType(eventType));
	}

}
