package se.jsa.jles.internal;


public interface EventFieldIndex {

	class EventFieldId {
		private final EventTypeId eventTypeId;
		private final String fieldName;
		public EventFieldId(EventTypeId eventTypeId, String fieldName) {
			this.eventTypeId = eventTypeId;
			this.fieldName = fieldName;
		}
		public EventTypeId getEventTypeId() {
			return eventTypeId;
		}
		public String getFieldName() {
			return fieldName;
		}
		@Override
		public int hashCode() {
			return (int) eventTypeId.toLong() * 31 + fieldName.hashCode();
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

	public EventTypeId getEventTypeId();
	public EventFieldId getFieldId();
	public boolean indexes(EventTypeId eventTypeId);

	public Iterable<EventId> getIterable(FieldConstraint constraint);
	public void onNewEvent(long eventId, Object event);

	public void prepare(EventIndexPreparation preparation);
	public void close();

}
