package se.jsa.jles.internal;

public class EventTypeId {

	private final long eventTypeId;

	public EventTypeId(long eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public long toLong() {
		return eventTypeId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EventTypeId)) {
			return false;
		}
		EventTypeId other = (EventTypeId)obj;
		return eventTypeId == other.eventTypeId;
	}

	@Override
	public int hashCode() {
		return (int) eventTypeId;
	}

	@Override
	public String toString() {
		return "EventTypeId [eventTypeId=" + eventTypeId + "]";
	}

}
