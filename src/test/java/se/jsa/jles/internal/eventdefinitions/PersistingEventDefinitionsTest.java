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
package se.jsa.jles.internal.eventdefinitions;

import java.io.File;
import java.util.Collections;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.EventTypeId;
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
		eventDefinitionsFile.write(new EventDefinition(new EventTypeId(1L), NonSerializableEvent.SerializableEventV2.class, Collections.<EventField>emptyList()));

		new MappingEventDefinitions(new PersistingEventDefinitions(eventDefinitionsFile)).init();
	}

	@Test
	public void acceptsCorrectEventDefinitions() throws Exception {
		eventDefinitionsFile.write(new EventDefinition(new EventTypeId(1L), NonSerializableEvent.SerializableEventV2.class, new EventFieldFactory().fromEventType(NonSerializableEvent.SerializableEventV2.class)));

		new MappingEventDefinitions(new PersistingEventDefinitions(eventDefinitionsFile));
	}

}
