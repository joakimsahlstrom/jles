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
package se.jsa.jles.configuration;

import se.jsa.jles.EventStoreConfigurer.EventFieldIndexConfiguration;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.indexing.EventFieldIndex;
import se.jsa.jles.internal.indexing.EventIndexPreparation;
import se.jsa.jles.internal.indexing.InMemoryEventFieldIndex;
import se.jsa.jles.internal.indexing.SimpleEventFieldIndex;
import se.jsa.jles.internal.indexing.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.util.Objects;

public class EventFieldIndexFactory {

	private final EventDefinitions eventDefinitions;
	private final EventIndexPreparation preparation;
	private final EntryFileNameGenerator entryFileNameGenerator;
	private final EntryFileFactory entryFileFactory;

	public EventFieldIndexFactory(EventDefinitions eventDefinitions,
			EventIndexPreparation preparation,
			EntryFileNameGenerator entryFileNameGenerator,
			EntryFileFactory entryFileFactory) {
		this.entryFileFactory = entryFileFactory;
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		this.preparation = Objects.requireNonNull(preparation);
		this.entryFileNameGenerator = Objects.requireNonNull(entryFileNameGenerator);
	}

	public EventFieldIndex createEventFieldIndex(EventFieldIndexConfiguration eventFieldIndexConfiguration, EventTypeId eventTypeId) {
		EventFieldIndex eventFieldIndex = createEventFieldIndex(
				eventFieldIndexConfiguration,
				eventTypeId,
				eventDefinitions.getEventField(eventTypeId, eventFieldIndexConfiguration.getFieldName()));
		eventFieldIndex.prepare(preparation);
		return eventFieldIndex;
	}

	private EventFieldIndex createEventFieldIndex(EventFieldIndexConfiguration eventFieldIndexConfiguration, EventTypeId eventTypeId, EventField eventField) {
		if (eventFieldIndexConfiguration.inMemory()) {
			return new InMemoryEventFieldIndex(
					new EventFieldId(eventTypeId, eventField.getPropertyName()),
					eventField,
					preparation.getEventTypeIndex(),
					eventDefinitions,
					preparation.getEventFile());
		} else {
			return new SimpleEventFieldIndex(
					eventTypeId,
					eventField,
					entryFileFactory.createEntryFile(entryFileNameGenerator.getEventFieldIndexFileName(eventTypeId, eventFieldIndexConfiguration.getFieldName())));
		}
	}

}
