package se.jsa.jles.internal;

public class EventIndex {

	private final long eventIndex;
	private final long eventIdByType;

	public EventIndex(long eventIndex, long eventIdByType) {
		this.eventIndex = eventIndex;
		this.eventIdByType = eventIdByType;
	}

	public long getEventIndex() {
		return eventIndex;
	}

	public long getEventIdByType() {
		return eventIdByType;
	}

}
