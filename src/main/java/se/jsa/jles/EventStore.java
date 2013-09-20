package se.jsa.jles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFieldConstraint;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.EventIndexIterable;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.LoadingIterable;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.util.Objects;

public class EventStore {

	final EventFile eventFile;
	final IndexFile eventTypeIndex;
	final EventDefinitions eventDefinitions;

	EventStore(EventFile eventFile, IndexFile eventTypeIndex) {
		this(eventFile, eventTypeIndex, new MappingEventDefinitions(new MemoryBasedEventDefinitions()));
	}

	EventStore(EventFile eventFile, IndexFile eventTypeIndex, EventDefinitions eventDefinitions) {
		this.eventFile = eventFile;
		this.eventTypeIndex = eventTypeIndex;
		this.eventDefinitions = eventDefinitions;
	}

	public static final EventStore create(FileChannelFactory fileChannelFactory) {
		EventFile eventFile = new EventFile(new FlippingEntryFile("events.ef", fileChannelFactory, true));
		IndexFile eventTypeIndex = new IndexFile(new StorableLongField(), new FlippingEntryFile("events.if", fileChannelFactory, true));
		EventDefinitionFile eventDefinitionFile = new EventDefinitionFile(new FlippingEntryFile("events.def", fileChannelFactory, true));
		EventDefinitions eventDefinitions = new MappingEventDefinitions(new PersistingEventDefinitions(eventDefinitionFile));
		return new EventStore(eventFile, eventTypeIndex, eventDefinitions);
	}

	public void init() {
		eventDefinitions.init();
	}

	public void write(Object event) {
		EventSerializer ed = eventDefinitions.getEventSerializer(event);
		long eventIndex = eventFile.writeEvent(event, ed);
		eventTypeIndex.writeIndex(eventIndex, ed.getEventTypeId());
	}

	public List<Object> collectEvents(Class<?>... eventTypes) {
		return collect(readEvents(eventTypes));
	}

	public static List<Object> collect(Iterable<Object> iterable) {
		List<Object> result = new ArrayList<Object>();
		for (Object event : iterable) {
			result.add(event);
		}
		return result;
	}

	public Iterable<Object> readEvents(Class<?>... eventTypes) {
		LoadingIterable loadingIterable = new LoadingIterable();
		for (Long eventTypeId : eventDefinitions.getEventTypeIds(eventTypes)) {
			InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
			Iterable<EventIndex> iterable = typedEventRepo.getIterator(new EventFieldConstraint());
			loadingIterable.register(iterable, typedEventRepo);
		}
		return loadingIterable;
	}

	public Iterable<Object> readEvents(Class<?> eventType, Match match) {
		LoadingIterable loadingIterable = new LoadingIterable();
		for (Long eventTypeId : eventDefinitions.getEventTypeIds(eventType)) {
			InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
			Iterable<EventIndex> iterable = match.buildFilteringIterator(typedEventRepo);
			loadingIterable.register(iterable, typedEventRepo);
		}
		return loadingIterable;
	}

	private class InternalTypedEventRepo implements TypedEventRepo {
		private final Long eventTypeId;
		private final EventDeserializer eventDeserializer;

		public InternalTypedEventRepo(Long eventTypeId) {
			this.eventTypeId = Objects.requireNonNull(eventTypeId);
			this.eventDeserializer = eventDefinitions.getEventDeserializer(eventTypeId);
		}

		@Override
		public Iterable<EventIndex> getIterator(EventFieldConstraint constraint) {
			if (constraint.hasConstraint()) {
				throw new RuntimeException("Not supported!");
			}

			return new EventIndexIterable(eventTypeIndex.readIndicies(Long.class, new EventTypeMatcher(eventTypeId)));
		}

		@Override
		public Object readEvent(EventIndex eventIndex) {
			return eventFile.readEvent(eventIndex.getEventIndex(), eventDeserializer);
		}

		@Override
		public Object readEventField(EventIndex eventIndex, String fieldName) {
			return eventDefinitions.getEventField(eventTypeId, fieldName).getValue(readEvent(eventIndex)); // non indexed
		}

		@Override
		public Indexing getIndexing(String fieldName) {
			return fieldName.equals("First") ? Indexing.SIMPLE : Indexing.NONE;
		}

	}

	private class EventTypeMatcher implements IndexKeyMatcher<Long> {
		private final Set<Long> acceptedTypes;
		public EventTypeMatcher(Set<Long> acceptedEventTypes) {
			this.acceptedTypes = acceptedEventTypes;
		}
		public EventTypeMatcher(Long eventTypeId) {
			this(Collections.singleton(eventTypeId));
		}
		@Override
		public boolean accepts(Long t) {
			return acceptedTypes.contains(t);
		}
	}

}
