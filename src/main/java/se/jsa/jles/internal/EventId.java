package se.jsa.jles.internal;

public class EventId {

	private final long eventId;

	public EventId(long eventId) {
		this.eventId = eventId;
	}

	public long toLong() {
		return eventId;
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
		return eventId == other.eventId;
	}

	@Override
	public int hashCode() {
		return (int) eventId;
	}

	@Override
	public String toString() {
		return "EventId [eventId=" + eventId + "]";
	}


}
