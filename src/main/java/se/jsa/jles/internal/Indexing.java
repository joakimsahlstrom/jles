package se.jsa.jles.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import se.jsa.jles.internal.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.util.Objects;

/**
 * The class responsible for maintaining the overall indexing of jles
 * @author joakim Joakim Sahlström
 *
 */
public class Indexing {
	private final IndexFile eventTypeIndexFile;
	private final Map<Long, EventIndex> eventIndicies;
	private final Map<EventFieldId, EventFieldIndex> eventFieldIds;

	public Indexing(EntryFile eventTypeIndexFile, Map<Long, EventIndex> eventIndicies, Map<EventFieldId, EventFieldIndex> eventFieldIds) {
		this.eventTypeIndexFile = new IndexFile(new StorableLongField(), eventTypeIndexFile);
		this.eventIndicies = Objects.requireNonNull(eventIndicies);
		this.eventFieldIds = Objects.requireNonNull(eventFieldIds);
	}

	public Iterable<EventId> readIndicies(Long eventTypeId, EventFieldConstraint constraint, TypedEventRepo typedEventRepo) {
		if (!constraint.hasConstraint()) {
			return getIndexEntryIterable(eventTypeId);
		}
		EventFieldId eventFieldId = new EventFieldId(eventTypeId, constraint.getFieldName());
		if (eventFieldIds.containsKey(eventFieldId)) {
			return eventFieldIds.get(eventFieldId).getIterable(constraint);
		} else {
			Iterable<EventId> baseIter = getIndexEntryIterable(eventTypeId);
			return new FallbackFilteringEventIdIterable(baseIter, constraint, typedEventRepo);
		}
	}

	private Iterable<EventId> getIndexEntryIterable(Long eventTypeId) {
		if (eventIndicies.containsKey(eventTypeId)) {
			return eventIndicies.get(eventTypeId).readIndicies();
		}
		return eventTypeIndexFile.readIndicies(new EventTypeMatcher(eventTypeId));
	}

	public void onNewEvent(long eventId, EventSerializer ed, Object event) {
		eventTypeIndexFile.writeIndex(eventId, ed.getEventTypeId());
		if (eventIndicies.containsKey(ed.getEventTypeId())) {
			eventIndicies.get(ed.getEventTypeId()).writeIndex(eventId);
		}
		for (EventFieldIndex efi : eventFieldIds.values()) {
			if (efi.indexes(ed.getEventTypeId())) {
				efi.onNewEvent(eventId, event);
			}
		}
	}

	public void stop() {
		eventTypeIndexFile.close();
		for (EventIndex ei : eventIndicies.values()) {
			ei.close();
		}
		for (EventFieldIndex efi : eventFieldIds.values()) {
			efi.close();
		}
	}

	private class EventTypeMatcher implements IndexKeyMatcher {
		private final Set<Long> acceptedTypes;
		public EventTypeMatcher(Set<Long> acceptedEventTypes) {
			this.acceptedTypes = acceptedEventTypes;
		}
		public EventTypeMatcher(Long eventTypeId) {
			this(Collections.singleton(eventTypeId));
		}
		@Override
		public boolean accepts(Object t) {
			return acceptedTypes.contains(Long.class.cast(t));
		}
	}

}
