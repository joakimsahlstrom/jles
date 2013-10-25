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

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EventId)) {
			return false;
		}
		EventId other = (EventId)obj;
		return eventId == other.eventId && eventIdByType == other.eventIdByType;
	}

	@Override
	public int hashCode() {
		return (int) (eventId * 997 + eventIdByType);
	}

	@Override
	public String toString() {
		return "EventId [eventId=" + eventId + ", eventIdByType=" + eventIdByType + "]";
	}

}
