package se.jsa.jles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFieldConstraint;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.IndexType;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.LoadingIterable;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.util.Objects;


/**
 * The class defining the basic interactions with jles
 * @author joakim Joakim Sahlström
 *
 */
public class EventStore {
	private final EventFile eventFile;
	private final Indexing indexing;
	private final EventDefinitions eventDefinitions;

	EventStore(EventFile eventFile, EntryFile eventTypeIndexFile) {
		this(eventFile,
			 new Indexing(new IndexFile(new StorableLongField(), eventTypeIndexFile), Collections.<Long, EventIndex>emptyMap(), Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap()),
			 new MappingEventDefinitions(new MemoryBasedEventDefinitions()));
	}

	EventStore(EventFile eventFile, Indexing indexing, EventDefinitions eventDefinitions) {
		this.eventFile = eventFile;
		this.indexing = indexing;
		this.eventDefinitions = eventDefinitions;
	}

	public void write(Object event) {
		EventSerializer ed = eventDefinitions.getEventSerializer(event);
		long eventId = eventFile.writeEvent(event, ed);
		indexing.onNewEvent(eventId, ed, event);
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
			Iterable<EventId> iterable = typedEventRepo.getIterator(EventFieldConstraint.none());
			loadingIterable.register(iterable, typedEventRepo);
		}
		return loadingIterable;
	}

	public Iterable<Object> readEvents(Class<?> eventType, Match match) {
		LoadingIterable loadingIterable = new LoadingIterable();
		for (Long eventTypeId : eventDefinitions.getEventTypeIds(eventType)) {
			InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
			Iterable<EventId> iterable = match.buildFilteringIterator(typedEventRepo);
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
		public Iterable<EventId> getIterator(EventFieldConstraint constraint) {
			return indexing.readIndicies(eventTypeId, constraint, this);
		}

		@Override
		public Object readEvent(EventId eventIndex) {
			return eventFile.readEvent(eventIndex.getEventId(), eventDeserializer);
		}

		@Override
		public Object readEventField(EventId eventIndex, String fieldName) {
			return eventDefinitions.getEventField(eventTypeId, fieldName).getValue(readEvent(eventIndex)); // non indexed
		}

		@Override
		public IndexType getIndexing(String fieldName) {
			return fieldName.equals("First") ? IndexType.SIMPLE : IndexType.NONE;
		}

	}

	public void stop() {
		eventFile.close();
		indexing.stop();
		eventDefinitions.close();
	}

}
