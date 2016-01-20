package se.jsa.jles.internal;


public interface EventFieldIndex {

	class EventFieldId {
		private final long eventTypeId;
		private final String fieldName;
		public EventFieldId(long eventTypeId, String fieldName) {
			this.eventTypeId = eventTypeId;
			this.fieldName = fieldName;
		}
		public long getEventTypeId() {
			return eventTypeId;
		}
		public String getFieldName() {
			return fieldName;
		}
		@Override
		public int hashCode() {
			return (int) eventTypeId * 31 + fieldName.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof EventFieldId)) {
				return false;
			}
			EventFieldId other = (EventFieldId) obj;
			return eventTypeId == other.eventTypeId && fieldName.equals(other.fieldName);
		}
	}

	public long getEventTypeId();
	public EventFieldId getFieldId();
	public boolean indexes(long eventTypeId);

	public Iterable<EventId> getIterable(FieldConstraint constraint);
	public void onNewEvent(long eventId, Object event);

	public void close();

}
