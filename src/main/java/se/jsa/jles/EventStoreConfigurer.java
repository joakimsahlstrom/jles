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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import se.jsa.jles.configuration.EntryFileFactory;
import se.jsa.jles.configuration.EntryFileNameGenerator;
import se.jsa.jles.configuration.EventFieldIndexFactory;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.EventIndexPreparation;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.InMemoryFileRepository;
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

//	private final FileChannelFactory fileChannelFactory;
//	private final InMemoryFileRepository inMemoryFileRepository;
	private final Set<Class<?>> indexedEventTypes = new HashSet<Class<?>>();
	private final Set<EventFieldIndexConfiguration> indexedEventFields = new HashSet<EventFieldIndexConfiguration>();
	private boolean useFileBasedEventDefinitions;
	private final AtomicReference<Boolean> multiThreadedEnvironment = new AtomicReference<Boolean>(Boolean.TRUE);
//	private WriteStrategy writeStrategy = WriteStrategy.FAST;

//	private final List<String> files = new ArrayList<String>();
	private final EntryFileFactory entryFileFactory;

	private EventStoreConfigurer(InMemoryFileRepository inMemoryFileRepository) {
		this.entryFileFactory = new EntryFileFactory(null, inMemoryFileRepository, multiThreadedEnvironment);
		this.useFileBasedEventDefinitions = true;
	}

	private EventStoreConfigurer(FileChannelFactory fileChannelFactory) {
		this.entryFileFactory = new EntryFileFactory(fileChannelFactory, null, multiThreadedEnvironment);
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
		this.multiThreadedEnvironment.set(Boolean.TRUE);
		return this;
	}

	public EventStoreConfigurer singleThreadedEnvironment() {
		this.multiThreadedEnvironment.set(Boolean.FALSE);
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

		EventStore result = new EventStore(eventFile, indexing, eventDefinitions);
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
		EventIndexPreparationImpl eventIndexPreparer = new EventIndexPreparationImpl(eventTypeIndex, eventDefinitions, eventFile);
		HashMap<EventTypeId, EventIndex> eventIndicies = createEventIndicies(eventDefinitions, eventIndexPreparer);
		HashMap<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = createEventFieldIndicies(eventDefinitions, eventIndexPreparer);

		return new Indexing(eventTypeIndex, eventIndicies, eventFieldIndicies, multiThreadedEnvironment.get());
	}

	private HashMap<EventTypeId, EventIndex> createEventIndicies(EventDefinitions eventDefinitions, EventIndexPreparationImpl preparation) {
		HashMap<EventTypeId, EventIndex> eventIndicies = new HashMap<EventTypeId, EventIndex>();
		for (Class<?> indexedEventType : indexedEventTypes) {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(indexedEventType)) {
				EventIndex eventIndex = new EventIndex(entryFileFactory.createEntryFile(entryFileNameGenerator.getEventIndexFileName(eventTypeId)), eventTypeId);
				preparation.prepare(eventIndex);
				eventIndicies.put(eventTypeId, eventIndex);
			}
		}
		return eventIndicies;
	}

	private HashMap<EventFieldIndex.EventFieldId, EventFieldIndex> createEventFieldIndicies(EventDefinitions eventDefinitions, EventIndexPreparation preparation) {
		EventFieldIndexFactory eventFieldIndexFactory = new EventFieldIndexFactory(eventDefinitions, preparation, entryFileNameGenerator, entryFileFactory);

		HashMap<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = new HashMap<EventFieldIndex.EventFieldId, EventFieldIndex>();
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

	private class EventIndexPreparationImpl implements EventIndexPreparation {
		private final IndexFile eventTypeIndex;
		private final EventFile eventFile;
		private final EventDefinitions eventDefinitions;
		private final ExecutorService executorService = Executors.newSingleThreadExecutor();

		public EventIndexPreparationImpl(IndexFile eventTypeIndex, EventDefinitions eventDefinitions, EventFile eventFile) {
			this.eventTypeIndex = Objects.requireNonNull(eventTypeIndex);
			this.eventFile = Objects.requireNonNull(eventFile);
			this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		}

		@Override
		public EventFile getEventFile() {
			return eventFile;
		}

		@Override
		public IndexFile getEventTypeIndex() {
			return eventTypeIndex;
		}

		@Override
		public EventDefinitions getEventDefinitions() {
			return eventDefinitions;
		}

		@Override
		public void schedule(Runnable runnable) {
			executorService.submit(runnable);
		}

		public void prepare(EventIndex index) {
			Iterator<EventId> existingIndicies = index.readIndicies().iterator();
			Iterator<EventId> sourceIndicies = eventTypeIndex.readIndicies(new Indexing.EventTypeMatcher(index.getEventTypeId())).iterator();
			while (existingIndicies.hasNext()) {
				if (!sourceIndicies.hasNext()) {
					throw new RuntimeException("Index for eventType " + index.getEventTypeId() + " contains more indexes than the source event type index");
				}
				EventId expected = sourceIndicies.next();
				EventId actual = existingIndicies.next();
				if (!expected.equals(actual)) {
					throw new RuntimeException("Indexing between event index and source event type index did not match for eventTypeId=" + index.getEventTypeId()
							+ ". Expected=" + expected
							+ " Actual=" + actual);
				}
			}
			while (sourceIndicies.hasNext()) {
				index.writeIndex(sourceIndicies.next().toLong());
			}
		}
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