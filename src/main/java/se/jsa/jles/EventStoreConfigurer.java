package se.jsa.jles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.SimpleEventFieldIndex;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.file.ThreadSafeEntryFile;
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

	private final FileChannelFactory fileChannelFactory;
	private final InMemoryFileRepository inMemoryFileRepository;
	private final Set<Class<?>> indexedEventTypes = new HashSet<Class<?>>();
	private final Set<EventFieldIndexConfiguration> indexedEventFields = new HashSet<EventFieldIndexConfiguration>();
	private boolean useFileBasedEventDefinitions;
	private boolean multiThreadedEnvironment;
	private WriteStrategy writeStrategy = WriteStrategy.FAST;

	private final List<String> files = new ArrayList<String>();

	private EventStoreConfigurer(InMemoryFileRepository inMemoryFileRepository) {
		this.fileChannelFactory = null;
		this.inMemoryFileRepository = inMemoryFileRepository;
		this.useFileBasedEventDefinitions = true;
	}

	private EventStoreConfigurer(FileChannelFactory fileChannelFactory) {
		this.fileChannelFactory = fileChannelFactory;
		this.inMemoryFileRepository = null;
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
		this.indexedEventFields.add(new EventFieldIndexConfiguration(eventType, fieldName));
		return this;
	}

	public EventStoreConfigurer multiThreadedEnvironment() {
		this.multiThreadedEnvironment = true;
		return this;
	}

	public EventStoreConfigurer singleThreadedEnvironment() {
		this.multiThreadedEnvironment = false;
		return this;
	}

	public EventStoreConfigurer testableEventDefinitions() {
		this.useFileBasedEventDefinitions = false;
		return this;
	}

	public EventStoreConfigurer writeStrategy(WriteStrategy writeStrategy) {
		this.writeStrategy = writeStrategy;
		return this;
	}

	public EventStore configure() {
		EntryFile eventTypeIndexFile = createEntryFile(entryFileNameGenerator.getEventTypeIndexFileName());
		EventFile eventFile = new EventFile(createEntryFile(entryFileNameGenerator.getEventFileName()));
		EventDefinitions eventDefinitions = createEventDefinitions();
		Indexing indexing = createIndexing(eventTypeIndexFile, eventDefinitions, eventFile);

		EventStore result = new EventStore(eventFile, indexing, eventDefinitions);
		return result;
	}

	private EventDefinitions createEventDefinitions() {
		if (useFileBasedEventDefinitions) {
			PersistingEventDefinitions persistedEventDefinitions = new PersistingEventDefinitions(
					new EventDefinitionFile(createEntryFile(entryFileNameGenerator.getEventDefintionsFileName())));
			MappingEventDefinitions eventDefinitions = new MappingEventDefinitions(persistedEventDefinitions);
			eventDefinitions.init();
			return eventDefinitions;
		} else {
			return new MappingEventDefinitions(new MemoryBasedEventDefinitions());
		}
	}

	private Indexing createIndexing(EntryFile eventTypeIndexFile, EventDefinitions eventDefinitions, EventFile eventFile) {
		IndexFile eventTypeIndex = new IndexFile(new StorableLongField(), eventTypeIndexFile);
		EventIndexPreparer eventIndexPreparer = new EventIndexPreparer(eventTypeIndex, eventDefinitions, eventFile);
		HashMap<Long, EventIndex> eventIndicies = createEventIndicies(eventDefinitions, eventIndexPreparer);
		HashMap<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = createEventFieldIndicies(eventDefinitions, eventIndexPreparer);

		return new Indexing(eventTypeIndex, eventIndicies, eventFieldIndicies, multiThreadedEnvironment);
	}

	private HashMap<EventFieldIndex.EventFieldId, EventFieldIndex> createEventFieldIndicies(EventDefinitions eventDefinitions, EventIndexPreparer eventIndexPreparer) {
		HashMap<EventFieldIndex.EventFieldId, EventFieldIndex> eventFieldIndicies = new HashMap<EventFieldIndex.EventFieldId, EventFieldIndex>();
		for (EventFieldIndexConfiguration eventFieldIndexConfiguration : indexedEventFields) {
			for (Long eventTypeId : eventDefinitions.getEventTypeIds(eventFieldIndexConfiguration.getEventType())) {
				EventFieldIndex eventFieldIndex = new SimpleEventFieldIndex(
						eventTypeId,
						eventDefinitions.getEventField(eventTypeId, eventFieldIndexConfiguration.getFieldName()),
						createEntryFile(entryFileNameGenerator.getEventFieldIndexFileName(eventTypeId, eventFieldIndexConfiguration.getFieldName())));
				eventIndexPreparer.prepare(eventFieldIndex);
				eventFieldIndicies.put(eventFieldIndexConfiguration.createEventFieldId(eventTypeId), eventFieldIndex);
			}
		}
		return eventFieldIndicies;
	}

	private HashMap<Long, EventIndex> createEventIndicies(EventDefinitions eventDefinitions, EventIndexPreparer eventIndexPreparer) {
		HashMap<Long, EventIndex> eventIndicies = new HashMap<Long, EventIndex>();
		for (Class<?> indexedEventType : indexedEventTypes) {
			for (Long eventTypeId : eventDefinitions.getEventTypeIds(indexedEventType)) {
				EventIndex eventIndex = new EventIndex(createEntryFile(entryFileNameGenerator.getEventIndexFileName(eventTypeId)), eventTypeId);
				eventIndexPreparer.prepare(eventIndex);
				eventIndicies.put(eventTypeId, eventIndex);
			}
		}
		return eventIndicies;
	}

	private EntryFile createEntryFile(String fileName) {
		if (fileChannelFactory == null) {
			return inMemoryFileRepository.getEntryFile(fileName);
		}

		EntryFile flippingEntryFile = new FlippingEntryFile(fileName, fileChannelFactory, writeStrategy);
		if (multiThreadedEnvironment) {
			flippingEntryFile = new ThreadSafeEntryFile(flippingEntryFile);
		}
		files.add(fileName);
		return flippingEntryFile;
	}

	public List<String> getFiles() {
		return files;
	}

	private class EventIndexPreparer {
		private final IndexFile eventTypeIndex;
		private final EventFile eventFile;
		private final EventDefinitions eventDefinitions;

		public EventIndexPreparer(IndexFile eventTypeIndex, EventDefinitions eventDefinitions, EventFile eventFile) {
			this.eventTypeIndex = Objects.requireNonNull(eventTypeIndex);
			this.eventFile = Objects.requireNonNull(eventFile);
			this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		}

		public void prepare(EventIndex index) {
			Iterator<EventId> existingIndicies = index.readIndicies().iterator();
			Iterator<EventId> sourceIndicies = eventTypeIndex.readIndicies(new Indexing.EventTypeMatcher(index.getEventTypeId())).iterator();
			while (existingIndicies.hasNext()) {
				if (!sourceIndicies.hasNext()) {
					throw new RuntimeException("Index for eventType " + index.getEventTypeId() + " contains more indexes than the source event type index");
				}
				if (!existingIndicies.next().equals(sourceIndicies.next())) {
					throw new RuntimeException("Indexing between event index and source event type index did not match for eventType " + index.getEventTypeId());
				}
			}
			while (sourceIndicies.hasNext()) {
				index.writeIndex(sourceIndicies.next().getEventId());
			}
		}

		public void prepare(EventFieldIndex eventFieldIndex) {
			Iterator<EventId> existingIndicies = eventFieldIndex.getIterable(FieldConstraint.noConstraint()).iterator();
			Iterator<EventId> sourceIndicies = eventTypeIndex.readIndicies(new Indexing.EventTypeMatcher(eventFieldIndex.getEventTypeId())).iterator();
			while (existingIndicies.hasNext()) {
				if (!sourceIndicies.hasNext()) {
					throw new RuntimeException("Index for eventType " + eventFieldIndex.getEventTypeId() + " contains more indexes than the source event type index");
				}
				if (!existingIndicies.next().equals(sourceIndicies.next())) {
					throw new RuntimeException("Indexing between event index and source event type index did not match for eventType " + eventFieldIndex.getEventTypeId());
				}
			}
			EventDeserializer eventDeserializer = eventDefinitions.getEventDeserializer(eventFieldIndex.getEventTypeId());
			while (sourceIndicies.hasNext()) {
				EventId eventId = sourceIndicies.next();
				eventFieldIndex.onNewEvent(eventId.getEventId(), eventFile.readEvent(eventId.getEventId(), eventDeserializer));
			}
		}
	}

	private class EventFieldIndexConfiguration {
		private final Class<?> eventType;
		private final String fieldName;

		public EventFieldIndexConfiguration(Class<?> eventType, String fieldName) {
			this.eventType = Objects.requireNonNull(eventType);
			this.fieldName = Objects.requireNonNull(fieldName);
		}

		public EventFieldId createEventFieldId(Long eventTypeId) {
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