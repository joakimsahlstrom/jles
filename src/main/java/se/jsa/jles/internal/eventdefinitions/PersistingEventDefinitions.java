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

import java.util.Collections;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class PersistingEventDefinitions implements EventDefinitions, MemoryBasedEventDefinitions.EventDefinitionsListener {

	private final EventDefinitionFile eventDefinitionFile;
	private final MemoryBasedEventDefinitions eventDefinitionCache;

	public PersistingEventDefinitions(EventDefinitionFile eventDefinitionFile) {
		this.eventDefinitionFile = Objects.requireNonNull(eventDefinitionFile);
		this.eventDefinitionCache = new MemoryBasedEventDefinitions();
	}

	@Override
	public void init() {
		eventDefinitionCache.init(eventDefinitionFile.readAllEventDefinitions(), Collections.singleton(MemoryBasedEventDefinitions.Flag.VerifyDatamodel));
		this.eventDefinitionCache.addListener(this);
	}

	@Override
	public void close() {
		eventDefinitionFile.close();
	}

	@Override
	public Class<?>[] getRegisteredEventTypes() {
		return eventDefinitionCache.getRegisteredEventTypes();
	}

	@Override
	public EventDeserializer getEventDeserializer(EventTypeId eventTypeId) {
		return eventDefinitionCache.getEventDeserializer(eventTypeId);
	}

	@Override
	public EventSerializer getEventSerializer(Object event) {
		return eventDefinitionCache.getEventSerializer(event);
	}

	@Override
	public Set<EventTypeId> getEventTypeIds(Class<?>... eventTypes) {
		return eventDefinitionCache.getEventTypeIds(eventTypes);
	}

	@Override
	public EventField getEventField(EventTypeId eventTypeId, String fieldName) {
		return eventDefinitionCache.getEventField(eventTypeId, fieldName);
	}

	// ----- From MemoryBasedEventDefinitions.EventDefinitionsListener -----

	@Override
	public void onNewEventDefinition(EventDefinition eventDefinition) {
		eventDefinitionFile.write(eventDefinition);
	}

}
