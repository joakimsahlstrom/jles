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

import se.jsa.jles.configuration.EventFieldIndexingFactory.EventFieldIndexConfiguration;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.indexing.EventIndexPreparation;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.indexing.fields.InMemoryEventFieldIndex;
import se.jsa.jles.internal.indexing.fields.SimpleEventFieldIndex;
import se.jsa.jles.internal.util.Objects;

public class EventFieldIndexFactory {

	private final EventFieldFactory eventFieldFactory = new EventFieldFactory();
	private final EventIndexPreparation preparation;
	private final EntryFileNameGenerator entryFileNameGenerator;
	private final EntryFileFactoryConfiguration entryFileFactory;

	public EventFieldIndexFactory(EventIndexPreparation preparation,
			EntryFileNameGenerator entryFileNameGenerator,
			EntryFileFactoryConfiguration entryFileFactory) {
		this.entryFileFactory = entryFileFactory;
		this.preparation = Objects.requireNonNull(preparation);
		this.entryFileNameGenerator = Objects.requireNonNull(entryFileNameGenerator);
	}

	public EventFieldIndex createEventFieldIndex(EventFieldIndexConfiguration eventFieldIndexConfiguration, EventTypeId eventTypeId) {
		EventFieldIndex eventFieldIndex = doCreateEventFieldIndex(eventFieldIndexConfiguration, eventTypeId);
		eventFieldIndex.prepare(preparation);
		return eventFieldIndex;
	}

	private EventFieldIndex doCreateEventFieldIndex(EventFieldIndexConfiguration eventFieldIndexConfiguration, EventTypeId eventTypeId) {
		if (eventFieldIndexConfiguration.inMemory()) {
			return new InMemoryEventFieldIndex(
					new EventFieldId(eventTypeId, eventFieldIndexConfiguration.getFieldName()),
					preparation.getTypedEventRepo(eventTypeId),
					preparation.readIndicies(eventTypeId));
		} else {
			return new SimpleEventFieldIndex(
					eventTypeId,
					eventFieldFactory.createEventField(eventFieldIndexConfiguration.getEventType(), eventFieldIndexConfiguration.getFieldName()),
					entryFileFactory.createEntryFile(entryFileNameGenerator.getEventFieldIndexFileName(eventTypeId, eventFieldIndexConfiguration.getFieldName())));
		}
	}

}
