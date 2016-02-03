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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.indexing.EventIndexPreparation;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.indexing.fields.EventFieldIndexing;
import se.jsa.jles.internal.indexing.fields.EventFieldIndexingImpl;
import se.jsa.jles.internal.util.Objects;

public class EventFieldIndexingFactory {

	public static class EventFieldIndexConfiguration {
		private final Class<?> eventType;
		private final String fieldName;
		private final boolean inMemory;

		public EventFieldIndexConfiguration(Class<?> eventType, String fieldName, boolean inMemory) {
			this.eventType = Objects.requireNonNull(eventType);
			this.fieldName = Objects.requireNonNull(fieldName);
			this.inMemory = inMemory;
		}

		public boolean inMemory() {
			return inMemory;
		}

		public EventFieldId createEventFieldId(EventTypeId eventTypeId) {
			return new EventFieldId(eventTypeId, fieldName);
		}

		public String getFieldName() {
			return fieldName;
		}

		public Class<?> getEventType() {
			return eventType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof EventFieldIndexingFactory.EventFieldIndexConfiguration)) {
				return false;
			}
			EventFieldIndexingFactory.EventFieldIndexConfiguration other = (EventFieldIndexingFactory.EventFieldIndexConfiguration)obj;
			return eventType.equals(other.eventType) && fieldName.equals(other.fieldName);
		}

		@Override
		public int hashCode() {
			return eventType.hashCode() * 977 + fieldName.hashCode();
		}
	}
	
	private final EventFieldIndexFactory eventFieldIndexFactory;
	
	public EventFieldIndexingFactory(EventIndexPreparation preparation,
			EntryFileNameGenerator entryFileNameGenerator,
			EntryFileFactoryConfiguration entryFileFactory) {
		this.eventFieldIndexFactory = new EventFieldIndexFactory(preparation, entryFileNameGenerator, entryFileFactory);
	}
	
	public EventFieldIndexing createEventFieldIndicies(EventDefinitions eventDefinitions, 
			Collection<EventFieldIndexConfiguration> indexedEventFields) {
		Map<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = new HashMap<EventFieldIndex.EventFieldId, EventFieldIndex>();
		for (EventFieldIndexConfiguration eventFieldIndexConfiguration : indexedEventFields) {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(eventFieldIndexConfiguration.getEventType())) {
				EventFieldIndex eventFieldIndex = eventFieldIndexFactory.createEventFieldIndex(eventFieldIndexConfiguration, eventTypeId);
				eventFieldIndicies.put(eventFieldIndexConfiguration.createEventFieldId(eventTypeId), eventFieldIndex);
			}
		}
		return new EventFieldIndexingImpl(eventFieldIndicies);
	}
	
}
