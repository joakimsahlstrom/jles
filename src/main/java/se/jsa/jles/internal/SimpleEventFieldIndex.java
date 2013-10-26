package se.jsa.jles.internal;

import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.fields.EventField;

public class SimpleEventFieldIndex implements EventFieldIndex {

	private final EventFieldId eventFieldId;
	private final IndexFile entriesFile;

	public SimpleEventFieldIndex(Long eventTypeId, EventField eventField, EntryFile indexEntryFile) {
		this.eventFieldId = new EventFieldId(eventTypeId, eventField.getPropertyName());
		this.entriesFile = new IndexFile(eventField, indexEntryFile);
	}

	@Override
	public EventFieldId getFieldId() {
		return eventFieldId;
	}

	@Override
	public Long getEventTypeId() {
		return eventFieldId.getEventTypeId();
	}

	@Override
	public boolean indexes(long eventTypeId) {
		return eventFieldId.getEventTypeId().equals(eventTypeId);
	}

	@Override
	public Iterable<EventId> getIterable(final EventFieldConstraint constraint) {
		return entriesFile.readIndicies(new IndexKeyMatcher() {
			@Override
			public boolean accepts(Object t) {
				return constraint.accepts(t);
			}
		});
	}

	@Override
	public void onNewEvent(long eventId, Object event) {
		register(eventId, event);
	}

	private void register(long eventId, Object event) {
		entriesFile.writeIndex(eventId, event);
	}

	@Override
	public void close() {
		entriesFile.close();
	}

}