package se.jsa.jles.internal.eventdefinitions;

import java.io.File;
import java.util.Collections;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.file.SynchronousEntryFile;
import se.jsa.jles.internal.testevents.NonSerializableEvent;



public class PersistingEventDefinitionsTest {
	private final EventDefinitionFile eventDefinitionsFile = new EventDefinitionFile(new SynchronousEntryFile("events.def"));

	@After
	public void setup() {
		eventDefinitionsFile.close();
		delete("events.def");
	}

	private void delete(String fileName) {
		new File(fileName).delete();
	}

	@Test(expected = RuntimeException.class)
	public void doesNotAcceptNonConformingEventDefinitions() throws Exception {
		eventDefinitionsFile.write(new EventDefinition(1L, NonSerializableEvent.SerializableEventV2.class, Collections.<EventField>emptyList()));

		new MappingEventDefinitions(new PersistingEventDefinitions(eventDefinitionsFile)).init();
	}

	@Test
	public void acceptsCorrectEventDefinitions() throws Exception {
		eventDefinitionsFile.write(new EventDefinition(1L, NonSerializableEvent.SerializableEventV2.class, new EventFieldFactory().fromEventType(NonSerializableEvent.SerializableEventV2.class)));

		new MappingEventDefinitions(new PersistingEventDefinitions(eventDefinitionsFile));
	}

}
