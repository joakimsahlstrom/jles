package se.jsa.jles.internal;

public interface EventFieldIndex {

	class EventFieldId {
		private final Long eventTypeId;
		private final String fieldName;
		public EventFieldId(Long eventTypeId, String fieldName) {
			this.eventTypeId = eventTypeId;
			this.fieldName = fieldName;
		}
		public Long getEventTypeId() {
			return eventTypeId;
		}
		public String getFieldName() {
			return fieldName;
		}
		@Override
		public int hashCode() {
			return eventTypeId.hashCode() * 31 + fieldName.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof EventFieldIndex.EventFieldId)) {
				return false;
			}
			EventFieldIndex.EventFieldId other = (EventFieldIndex.EventFieldId)obj;
			return eventTypeId.equals(other.eventTypeId) && fieldName.equals(other.fieldName);
		}
	}

	public EventFieldId getFieldId();
	public boolean indexes(long eventTypeId);

	public Iterable<EventId> getIterable(EventFieldConstraint constraint);
	public void onNewEvent(long eventId, Object event);


}
