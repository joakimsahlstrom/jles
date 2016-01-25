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

import java.util.Iterator;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.indexing.IndexFile.IndexKeyMatcher;

public class SimpleEventFieldIndex implements EventFieldIndex {

	private final EventFieldId eventFieldId;
	private final IndexFile entriesFile;

	public SimpleEventFieldIndex(EventTypeId eventTypeId, EventField eventField, EntryFile indexEntryFile) {
		this.eventFieldId = new EventFieldId(eventTypeId, eventField.getPropertyName());
		this.entriesFile = new IndexFile(eventField, indexEntryFile);
	}

	@Override
	public EventFieldId getFieldId() {
		return eventFieldId;
	}

	@Override
	public EventTypeId getEventTypeId() {
		return eventFieldId.getEventTypeId();
	}

	@Override
	public boolean indexes(EventTypeId eventTypeId) {
		return eventFieldId.getEventTypeId().equals(eventTypeId);
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
		while (sourceIndicies.hasNext()) {
			EventId eventId = sourceIndicies.next();
			onNewEvent(eventId.toLong(), preparation.getTypedEventRepo(getEventTypeId()).readEvent(eventId));
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