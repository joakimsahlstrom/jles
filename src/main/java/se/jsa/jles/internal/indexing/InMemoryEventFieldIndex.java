/*
 * Copyright 2016 Joakim Sahlström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jsa.jles.internal.indexing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
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
	public EventTypeId getEventTypeId() {
		return eventFieldId.getEventTypeId();
	}

	@Override
	public EventFieldId getFieldId() {
		return eventFieldId;
	}

	@Override
	public boolean indexes(EventTypeId eventTypeId) {
		return getEventTypeId().equals(eventTypeId);
	}

	@Override
	public Iterable<EventId> getIterable(FieldConstraint fieldConstraint) {
		sync(); // todo: remove?
		return new EventFieldEntryIterable(entries, fieldConstraint);
	}

	@Override
	public void onNewEvent(long eventId, Object event) {
		sync();
	}

	@Override
	public void prepare(EventIndexPreparation preparation) {
		preparation.schedule(new Runnable() {
			@Override
			public void run() {
				sync();
			}
		});
	}

	@Override
	public void close() {
		// in-memory, do nothing
	}

	synchronized void sync() {
		while (eventIndicies.hasNext()) {
			EventId eventId = eventIndicies.next();
			Object fieldValue = eventFile.readEventField(eventId.toLong(), eventDeserializer, eventField);
			entries.add(new EventFieldEntry(eventId, fieldValue));
		}
	}

	@Override
	public String toString() {
		return "InMemoryEventFieldIndex [eventField=" + eventField + "]";
	}

}
