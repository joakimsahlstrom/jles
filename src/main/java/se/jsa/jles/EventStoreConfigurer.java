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
package se.jsa.jles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import se.jsa.jles.configuration.EntryFileFactory;
import se.jsa.jles.configuration.EntryFileNameGenerator;
import se.jsa.jles.configuration.EventFieldIndexFactory;
import se.jsa.jles.configuration.ThreadingEnvironment;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.indexing.EventFieldIndex;
import se.jsa.jles.internal.indexing.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.indexing.EventIndex;
import se.jsa.jles.internal.indexing.EventIndexPreparation;
import se.jsa.jles.internal.indexing.EventIndexPreparationImpl;
import se.jsa.jles.internal.indexing.IndexFile;
import se.jsa.jles.internal.indexing.Indexing;
import se.jsa.jles.internal.util.Objects;

/**
 * Used for creating fully initialized {@link EventStore} instances
 * @author joakim Joakim Sahlström
 */
public class EventStoreConfigurer {

	public enum WriteStrategy {
		FAST,
		SAFE,
		SUPERSAFE
	}

	private static EntryFileNameGenerator entryFileNameGenerator = new EntryFileNameGenerator();

	private final Set<Class<?>> indexedEventTypes = new HashSet<Class<?>>();
	private final Set<EventFieldIndexConfiguration> indexedEventFields = new HashSet<EventFieldIndexConfiguration>();
	private final AtomicReference<ThreadingEnvironment> threadingEnvironment = new AtomicReference<ThreadingEnvironment>(ThreadingEnvironment.MULTITHREADED);
	private final EntryFileFactory entryFileFactory;

	private boolean useFileBasedEventDefinitions;

	private EventStoreConfigurer(InMemoryFileRepository inMemoryFileRepository) {
		this.entryFileFactory = new EntryFileFactory(null, inMemoryFileRepository, threadingEnvironment);
		this.useFileBasedEventDefinitions = true;
	}

	private EventStoreConfigurer(FileChannelFactory fileChannelFactory) {
		this.entryFileFactory = new EntryFileFactory(fileChannelFactory, null, threadingEnvironment);
		this.useFileBasedEventDefinitions = true;
	}

	public static EventStoreConfigurer createMemoryOnlyConfigurer() {
		return new EventStoreConfigurer(new InMemoryFileRepository());
	}

	public static EventStoreConfigurer createMemoryOnlyConfigurer(InMemoryFileRepository inMemoryFileRepository) {
		return new EventStoreConfigurer(inMemoryFileRepository);
	}

	public static EventStoreConfigurer createFileBasedConfigurer(FileChannelFactory fileChannelFactory) {
		return new EventStoreConfigurer(fileChannelFactory);
	}

	public EventStoreConfigurer addIndexing(Class<?> eventType) {
		indexedEventTypes.add(eventType);
		return this;
	}

	public EventStoreConfigurer addIndexing(Class<?> eventType, String fieldName) {
		this.indexedEventFields.add(new EventFieldIndexConfiguration(eventType, fieldName, false));
		return this;
	}

	public EventStoreConfigurer addInMemoryIndexing(Class<?> eventType, String fieldName) {
		this.indexedEventFields.add(new EventFieldIndexConfiguration(eventType, fieldName, true));
		return this;
	}

	public EventStoreConfigurer multiThreadedEnvironment() {
		this.threadingEnvironment.set(ThreadingEnvironment.MULTITHREADED);
		return this;
	}

	public EventStoreConfigurer singleThreadedEnvironment() {
		this.threadingEnvironment.set(ThreadingEnvironment.SINGLE_THREAD);
		return this;
	}

	public EventStoreConfigurer testableEventDefinitions() {
		this.useFileBasedEventDefinitions = false;
		return this;
	}

	public EventStoreConfigurer writeStrategy(WriteStrategy writeStrategy) {
		this.entryFileFactory.setWriteStrategy(writeStrategy);
		return this;
	}

	public EventStore configure() {
		EntryFile eventTypeIndexFile = entryFileFactory.createEntryFile(entryFileNameGenerator.getEventTypeIndexFileName());
		EventFile eventFile = new EventFile(entryFileFactory.createEntryFile(entryFileNameGenerator.getEventFileName()));
		EventDefinitions eventDefinitions = createEventDefinitions();
		Indexing indexing = createIndexing(eventTypeIndexFile, eventDefinitions, eventFile);

		EventStore result = new EventStore(eventFile, indexing, eventDefinitions, threadingEnvironment.get());
		return result;
	}

	private EventDefinitions createEventDefinitions() {
		if (useFileBasedEventDefinitions) {
			PersistingEventDefinitions persistedEventDefinitions = new PersistingEventDefinitions(
					new EventDefinitionFile(entryFileFactory.createEntryFile(entryFileNameGenerator.getEventDefintionsFileName())));
			MappingEventDefinitions eventDefinitions = new MappingEventDefinitions(persistedEventDefinitions);
			eventDefinitions.init();
			return eventDefinitions;
		} else {
			return new MappingEventDefinitions(new MemoryBasedEventDefinitions());
		}
	}

	private Indexing createIndexing(EntryFile eventTypeIndexFile, EventDefinitions eventDefinitions, EventFile eventFile) {
		IndexFile eventTypeIndex = new IndexFile(new StorableLongField(), eventTypeIndexFile);
		EventIndexPreparation eventIndexPreparer = new EventIndexPreparationImpl(eventTypeIndex, eventDefinitions, eventFile);
		Map<EventTypeId, EventIndex> eventIndicies = createEventIndicies(eventDefinitions, eventIndexPreparer);
		Map<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = createEventFieldIndicies(eventDefinitions, eventIndexPreparer);

		return new Indexing(eventTypeIndex, eventIndicies, eventFieldIndicies, threadingEnvironment.get() == ThreadingEnvironment.MULTITHREADED);
	}

	private Map<EventTypeId, EventIndex> createEventIndicies(EventDefinitions eventDefinitions, EventIndexPreparation preparation) {
		Map<EventTypeId, EventIndex> eventIndicies = new HashMap<EventTypeId, EventIndex>();
		for (Class<?> indexedEventType : indexedEventTypes) {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(indexedEventType)) {
				EventIndex eventIndex = new EventIndex(entryFileFactory.createEntryFile(entryFileNameGenerator.getEventIndexFileName(eventTypeId)), eventTypeId);
				eventIndicies.put(eventTypeId, eventIndex);
			}
		}
		preparation.prepare(eventIndicies);
		return eventIndicies;
	}

	private Map<EventFieldIndex.EventFieldId, EventFieldIndex> createEventFieldIndicies(EventDefinitions eventDefinitions, EventIndexPreparation preparation) {
		EventFieldIndexFactory eventFieldIndexFactory = new EventFieldIndexFactory(eventDefinitions, preparation, entryFileNameGenerator, entryFileFactory);

		Map<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = new HashMap<EventFieldIndex.EventFieldId, EventFieldIndex>();
		for (EventFieldIndexConfiguration eventFieldIndexConfiguration : indexedEventFields) {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(eventFieldIndexConfiguration.getEventType())) {
				EventFieldIndex eventFieldIndex = eventFieldIndexFactory.createEventFieldIndex(eventFieldIndexConfiguration, eventTypeId);
				eventFieldIndicies.put(eventFieldIndexConfiguration.createEventFieldId(eventTypeId), eventFieldIndex);
			}
		}
		return eventFieldIndicies;
	}

	public List<String> getFiles() {
		return entryFileFactory.getFiles();
	}

	public class EventFieldIndexConfiguration {
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
			if (!(obj instanceof EventStoreConfigurer.EventFieldIndexConfiguration)) {
				return false;
			}
			EventStoreConfigurer.EventFieldIndexConfiguration other = (EventStoreConfigurer.EventFieldIndexConfiguration)obj;
			return eventType.equals(other.eventType) && fieldName.equals(other.fieldName);
		}

		@Override
		public int hashCode() {
			return eventType.hashCode() * 977 + fieldName.hashCode();
		}
	}

}