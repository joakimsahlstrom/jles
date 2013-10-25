package se.jsa.jles.internal;

import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.fields.EventField;

public class SimpleEventFieldIndex implements EventFieldIndex {

	private final EventFieldId eventFieldId;
	private final IndexFile entries;

	public SimpleEventFieldIndex(Long eventTypeId, EventField eventField, EntryFile indexEntryFile) {
		this.eventFieldId = new EventFieldId(eventTypeId, eventField.getPropertyName());
		this.entries = new IndexFile(eventField, indexEntryFile);
	}

	@Override
	public EventFieldId getFieldId() {
		return eventFieldId;
	}

	@Override
	public boolean indexes(long eventTypeId) {
		return eventFieldId.getEventTypeId().equals(eventTypeId);
	}

	@Override
	public Iterable<EventId> getIterable(final EventFieldConstraint constraint) {
		return entries.readIndicies(new IndexKeyMatcher() {
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
		entries.writeIndex(eventId, event);
	}

}