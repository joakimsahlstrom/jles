package se.jsa.jles.internal;

public class EventId {

	private final long eventId;
	private final long eventIdByType;

	public EventId(long eventId, long eventIdByType) {
		this.eventId = eventId;
		this.eventIdByType = eventIdByType;
	}

	public long getEventId() {
		return eventId;
	}

	public long getEventIdByType() {
		return eventIdByType;
	}

}
