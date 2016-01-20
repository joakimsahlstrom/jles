package se.jsa.jles.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;


public class InMemoryEventFieldIndex implements EventFieldIndex {

	private static class EventFieldEntry {
		private final EventId eventId;
		private final Object fieldValue;
		public EventFieldEntry(EventId eventId, Object fieldValue) {
			this.eventId = eventId;
			this.fieldValue = fieldValue;
		}
		public EventId getEventId() {
			return eventId;
		}
		public Object getFieldValue() {
			return fieldValue;
		}
	}

	private static class EventFieldEntryIterable implements Iterable<EventId> {
		private final List<EventFieldEntry> entries;
		private final FieldConstraint fieldConstraint;
		public EventFieldEntryIterable(List<EventFieldEntry> entries, FieldConstraint fieldConstraint) {
			this.entries = entries;
			this.fieldConstraint = fieldConstraint;
		}
		@Override
		public Iterator<EventId> iterator() {
			return new EventFieldEntryIterator(entries, fieldConstraint);
		}
	}

	private static class EventFieldEntryIterator implements Iterator<EventId> {
		private final List<EventFieldEntry> entries;
		private final FieldConstraint fieldConstraint;
		private EventFieldEntry next = null;
		private int pos = 0;

		public EventFieldEntryIterator(List<EventFieldEntry> entries, FieldConstraint fieldConstraint) {
			this.entries = entries;
			this.fieldConstraint = fieldConstraint;
		}
		@Override
		public boolean hasNext() {
			while (next == null && pos < entries.size()) {
				EventFieldEntry maybeNext = entries.get(pos++);
				if (fieldConstraint.accepts(maybeNext.getFieldValue())) {
					next = maybeNext;
				}
			}
			return next != null;
		}
		@Override
		public EventId next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			EventFieldEntry eId = next;
			next = null;
			return eId.getEventId();
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private final List<EventFieldEntry> entries = new ArrayList<EventFieldEntry>();
	private final EventFieldId eventFieldId;
	private final EventField eventField;
	private final Iterator<EventId> eventIndicies;
	private final EventDeserializer eventDeserializer;
	private final EventFile eventFile;

	public InMemoryEventFieldIndex(EventFieldId eventFieldId, EventField eventField, IndexFile eventTypeIndex, EventDefinitions eventDefinitions, EventFile eventFile) {
		this.eventFieldId = eventFieldId;
		this.eventField = eventField;
		this.eventIndicies = eventTypeIndex.readIndicies(new Indexing.EventTypeMatcher(eventFieldId.getEventTypeId())).iterator();
		this.eventDeserializer = eventDefinitions.getEventDeserializer(eventFieldId.getEventTypeId());
		this.eventFile = Objects.requireNonNull(eventFile);
	}

	@Override
	public long getEventTypeId() {
		return eventFieldId.getEventTypeId();
	}

	@Override
	public EventFieldId getFieldId() {
		return eventFieldId;
	}

	@Override
	public boolean indexes(long eventTypeId) {
		return getEventTypeId() == eventTypeId;
	}

	@Override
	public Iterable<EventId> getIterable(FieldConstraint fieldConstraint) {
		sync();
		return new EventFieldEntryIterable(entries, fieldConstraint);
	}

	@Override
	public void onNewEvent(long eventId, Object event) {
		sync();
	}

	@Override
	public void close() {
		// in-memory, do nothing
	}

	private synchronized void sync() {
		while (eventIndicies.hasNext()) {
			EventId eventId = eventIndicies.next();
			Object fieldValue = eventFile.readEventField(eventId.toLong(), eventDeserializer, eventField);
			entries.add(new EventFieldEntry(eventId, fieldValue));
		}
	}

}
