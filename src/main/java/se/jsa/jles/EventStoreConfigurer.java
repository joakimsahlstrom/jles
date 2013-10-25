package se.jsa.jles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.util.Objects;

/**
 * Used for creating fully initialized {@link EventStore} instances
 * @author joakim Joakim Sahlström
 */
public class EventStoreConfigurer {
	private final FileChannelFactory fileChannelFactory;
	private final Set<Class<?>> indexedEventTypes = new HashSet<Class<?>>();
	private boolean useFileBasedEventDefinitions;

	private final List<String> files = new ArrayList<String>();

	public EventStoreConfigurer(FileChannelFactory fileChannelFactory) {
		this.fileChannelFactory = fileChannelFactory;
		this.useFileBasedEventDefinitions = true;
	}

	public EventStoreConfigurer addIndexing(Class<?> eventType) {
		indexedEventTypes.add(eventType);
		return this;
	}

	public EventStoreConfigurer testableEventDefinitions() {
		this.useFileBasedEventDefinitions = false;
		return this;
	}

	public EventStore configure() {
		FlippingEntryFile eventTypeIndexFile = createEntryFile("events.if", fileChannelFactory);
		EventFile eventFile = new EventFile(createEntryFile("events.ef", fileChannelFactory));

		EventDefinitions eventDefinitions = createEventDefinitions();
		eventDefinitions.init();

		Indexing indexing = createIndexing(eventTypeIndexFile, eventDefinitions);

		EventStore result = new EventStore(eventFile, indexing, eventDefinitions);
		return result;
	}

	private EventDefinitions createEventDefinitions() {
		if (useFileBasedEventDefinitions) {
			return new MappingEventDefinitions(new PersistingEventDefinitions(new EventDefinitionFile(createEntryFile("events.def", fileChannelFactory))));
		} else {
			return new MappingEventDefinitions(new MemoryBasedEventDefinitions());
		}
	}

	private Indexing createIndexing(FlippingEntryFile eventTypeIndexFile, EventDefinitions eventDefinitions) {
		IndexFile eventTypeIndex = new IndexFile(new StorableLongField(), eventTypeIndexFile);
		EventIndexPreparer eventIndexPreparer = new EventIndexPreparer(eventTypeIndex);
		HashMap<Long, EventIndex> eventIndicies = new HashMap<Long, EventIndex>();
		for (Class<?> indexedEventType : indexedEventTypes) {
			for (Long eventTypeId : eventDefinitions.getEventTypeIds(indexedEventType)) {
				EventIndex eventIndex = new EventIndex(createEntryFile("events_" + eventTypeId + ".if", fileChannelFactory), eventTypeId);
				eventIndexPreparer.prepare(eventIndex);
				eventIndicies.put(eventTypeId, eventIndex);
			}
		}
		return new Indexing(eventTypeIndex, eventIndicies, Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap());
	}

	private FlippingEntryFile createEntryFile(String fileName, FileChannelFactory fileChannelFactory) {
		FlippingEntryFile flippingEntryFile = new FlippingEntryFile(fileName, fileChannelFactory);
		files.add(fileName);
		return flippingEntryFile;
	}

	public List<String> getFiles() {
		return files;
	}

	private class EventIndexPreparer {
		private final IndexFile eventTypeIndex;
		public EventIndexPreparer(IndexFile eventTypeIndex) {
			this.eventTypeIndex = Objects.requireNonNull(eventTypeIndex);
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
	}

}
