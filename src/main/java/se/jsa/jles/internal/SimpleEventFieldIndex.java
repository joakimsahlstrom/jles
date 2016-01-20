package se.jsa.jles.internal;

import java.util.Iterator;

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
	public long getEventTypeId() {
		return eventFieldId.getEventTypeId();
	}

	@Override
	public boolean indexes(long eventTypeId) {
		return eventFieldId.getEventTypeId() == eventTypeId;
	}

	@Override
	public Iterable<EventId> getIterable(final FieldConstraint constraint) {
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
	public void prepare(EventIndexPreparation preparation) {
		Iterator<EventId> existingIndicies = getIterable(FieldConstraint.noConstraint()).iterator();
		Iterator<EventId> sourceIndicies = preparation.getEventTypeIndex().readIndicies(new Indexing.EventTypeMatcher(getEventTypeId())).iterator();
		while (existingIndicies.hasNext()) {
			if (!sourceIndicies.hasNext()) {
				throw new RuntimeException("Index for eventType " + getEventTypeId() + " contains more indexes than the source event type index");
			}
			if (!existingIndicies.next().equals(sourceIndicies.next())) {
				throw new RuntimeException("Indexing between event index and source event type index did not match for eventType " + getEventTypeId());
			}
		}
		EventDeserializer eventDeserializer = preparation.getEventDefinitions().getEventDeserializer(getEventTypeId());
		while (sourceIndicies.hasNext()) {
			EventId eventId = sourceIndicies.next();
			onNewEvent(eventId.toLong(), preparation.getEventFile().readEvent(eventId.toLong(), eventDeserializer));
		}
	}

	@Override
	public void close() {
		entriesFile.close();
	}

	@Override
	public String toString() {
		return "SimpleEventFieldIndex [eventFieldId=" + eventFieldId + "]";
	}

}