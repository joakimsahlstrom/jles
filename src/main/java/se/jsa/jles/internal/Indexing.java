package se.jsa.jles.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import se.jsa.jles.internal.IndexFile.IndexEntry;
import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.fields.StorableLongField;

public class Indexing {
	private final IndexFile fallbackIndexFile;
	private final Map<Long, EventIndex> eventIndicies;

	public Indexing(EntryFile fallbackIndexFile, Map<Long, EventIndex> eventIndicies) {
		this.fallbackIndexFile = new IndexFile(new StorableLongField(), fallbackIndexFile);
		this.eventIndicies = eventIndicies;
	}

	public Iterable<EventId> readIndicies(Long eventTypeId, EventFieldConstraint constraint, TypedEventRepo typedEventRepo) {
		if (!constraint.hasConstraint()) {
			return new EventIdIterable<Long>(getIndexEntryIterable(eventTypeId));
		}
		EventIdIterable<Long> baseIter = new EventIdIterable<Long>(getIndexEntryIterable(eventTypeId));
		return new FallbackFilteringEventIdIterable(baseIter, constraint, typedEventRepo);
	}

	private Iterable<IndexEntry<Long>> getIndexEntryIterable(Long eventTypeId) {
		if (eventIndicies.containsKey(eventTypeId)) {
			return eventIndicies.get(eventTypeId).readIndicies();
		}
		return fallbackIndexFile.readIndicies(Long.class, new EventTypeMatcher(eventTypeId));
	}

	public void onNewEvent(long eventId, EventSerializer ed) {
		fallbackIndexFile.writeIndex(eventId, ed.getEventTypeId());
		if (eventIndicies.containsKey(ed.getEventTypeId())) {
			eventIndicies.get(ed.getEventTypeId()).writeIndex(eventId);
		}
	}

	public void stop() {
		fallbackIndexFile.close();
		for (EventIndex ei : eventIndicies.values()) {
			ei.close();
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
