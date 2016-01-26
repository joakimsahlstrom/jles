package se.jsa.jles.internal.indexing.events;

import se.jsa.jles.internal.EventId;

public class EventIndexEntry {
	private final EventId eventId;
	private final Object eventKey;

	public EventIndexEntry(EventId eventId, Object eventKey) {
		this.eventId = eventId;
		this.eventKey = eventKey;
	}

	public Object getEventKey() {
		return eventKey;
	}

	public EventId getEventId() {
		return eventId;
	}

	@Override
	public String toString() {
		return "EventIndexEntry [eventId=" + eventId + ", eventKey=" + eventKey + "]";
	}
}
