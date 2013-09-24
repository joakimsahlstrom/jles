package se.jsa.jles.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.fields.StorableLongField;

public class Indexing {
	private final IndexFile fallbackIndexFile;
	private final Map<Long, EventIndex> eventIndicies;

	public Indexing(EntryFile fallbackIndexFile, Map<Long, EventIndex> eventIndicies) {
		this.fallbackIndexFile = new IndexFile(new StorableLongField(), fallbackIndexFile);
		this.eventIndicies = eventIndicies;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> EventIdIterable<T> readIndicies(Class<T> keyType, Long eventTypeId) {
		if (keyType.equals(Long.class) && eventIndicies.containsKey(eventTypeId)) {
			return (EventIdIterable<T>) new EventIdIterable<Long>(eventIndicies.get(eventTypeId).readIndicies());
		}
		if (keyType.equals(Long.class)) {
			return (EventIdIterable<T>) new EventIdIterable<Long>(fallbackIndexFile.readIndicies(Long.class, new EventTypeMatcher(eventTypeId)));
		}
		throw new RuntimeException("No indexing of type " + keyType + " for eventTypeId " + eventTypeId);
	}

	public void onNewEvent(long eventId, EventSerializer ed) {
		fallbackIndexFile.writeIndex(eventId, ed.getEventTypeId());
		if (eventIndicies.containsKey(ed.getEventTypeId())) {
			eventIndicies.get(ed.getEventTypeId()).writeIndex(eventId);
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

	public void stop() {
		fallbackIndexFile.close();
		for (EventIndex ei : eventIndicies.values()) {
			ei.close();
		}
	}

}
