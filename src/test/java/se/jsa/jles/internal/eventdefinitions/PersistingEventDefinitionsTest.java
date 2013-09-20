package se.jsa.jles.internal.eventdefinitions;

import java.io.File;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.jsa.jles.internal.eventdefinitions.EventDefinition;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.file.SynchronousEntryFile;
import se.jsa.jles.internal.testevents.NonSerializableEvent;



public class PersistingEventDefinitionsTest {

	@Before
	@After
	public void setup() {
		delete("events.def");
	}

	private void delete(String fileName) {
		new File(fileName).delete();
	}

	@Test(expected = RuntimeException.class)
	public void doesNotAcceptNonConformingEventDefinitions() throws Exception {
		EventDefinitionFile eventDefinitionsFile = buildEventDefinitionsFile();
		eventDefinitionsFile.write(new EventDefinition(1L, NonSerializableEvent.SerializableEventV2.class, Collections.<EventField>emptyList()));

		new MappingEventDefinitions(new PersistingEventDefinitions(buildEventDefinitionsFile())).init();
	}

	@Test
	public void acceptsCorrectEventDefinitions() throws Exception {
		EventDefinitionFile eventDefinitionsFile = buildEventDefinitionsFile();
		eventDefinitionsFile.write(new EventDefinition(1L, NonSerializableEvent.SerializableEventV2.class, new EventFieldFactory().fromEventType(NonSerializableEvent.SerializableEventV2.class)));

		new MappingEventDefinitions(new PersistingEventDefinitions(buildEventDefinitionsFile()));
	}

	private EventDefinitionFile buildEventDefinitionsFile() {
		return new EventDefinitionFile(new SynchronousEntryFile("events.def"));
	}

}
