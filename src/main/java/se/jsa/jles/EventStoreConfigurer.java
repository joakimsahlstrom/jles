package se.jsa.jles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.file.FlippingEntryFile;

public class EventStoreConfigurer {
	private final FileChannelFactory fileChannelFactory;
	private final EventFile eventFile;
	private final FlippingEntryFile fallbackIndexFile;
	private final Set<Class<?>> indexedEventTypes = new HashSet<Class<?>>();
	private EventDefinitions eventDefinitions;

	private final List<String> files = new ArrayList<String>();

	public EventStoreConfigurer(FileChannelFactory fileChannelFactory) {
		this.fileChannelFactory = fileChannelFactory;
		this.eventFile = new EventFile(createEntryFile("events.ef", fileChannelFactory));
		this.fallbackIndexFile = createEntryFile("events.if", fileChannelFactory);
		this.eventDefinitions = new MappingEventDefinitions(new PersistingEventDefinitions(new EventDefinitionFile(createEntryFile("events.def", fileChannelFactory))));
	}

	public EventStoreConfigurer addIndexing(Class<?> eventType) {
		indexedEventTypes.add(eventType);
		return this;
	}

	public EventStoreConfigurer testableEventDefinitions() {
		this.eventDefinitions = new MappingEventDefinitions(new MemoryBasedEventDefinitions());
		return this;
	}

	public EventStore configure() {
		return new EventStore(eventFile, createIndexing(eventDefinitions), eventDefinitions);
	}

	private Indexing createIndexing(EventDefinitions eventDefinitions) {
		HashMap<Long, EventIndex> eventIndicies = new HashMap<Long, EventIndex>();
		for (Long eventTypeId : eventDefinitions.getEventTypeIds(indexedEventTypes.toArray(new Class<?>[indexedEventTypes.size()]))) {
			eventIndicies.put(eventTypeId, new EventIndex(createEntryFile("events_" + eventTypeId + ".if", fileChannelFactory), eventTypeId));
		}
		return new Indexing(fallbackIndexFile, eventIndicies, Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap());
	}

	private FlippingEntryFile createEntryFile(String fileName, FileChannelFactory fileChannelFactory) {
		FlippingEntryFile flippingEntryFile = new FlippingEntryFile(fileName, fileChannelFactory);
		files.add(fileName);
		return flippingEntryFile;
	}

	public List<String> getFiles() {
		return files;
	}

}
