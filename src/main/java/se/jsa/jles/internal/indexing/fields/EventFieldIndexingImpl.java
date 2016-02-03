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
package se.jsa.jles.internal.indexing.fields;

import java.util.Collections;
import java.util.Map;

import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.util.Objects;

public class EventFieldIndexingImpl implements EventFieldIndexing {

	private final Map<EventFieldId, EventFieldIndex> eventFieldIndicies;
	
	public EventFieldIndexingImpl(Map<EventFieldId, EventFieldIndex> eventFieldIndicies) {
		this.eventFieldIndicies = Objects.requireNonNull(eventFieldIndicies);
	}

	public static EventFieldIndexing noIndexing() {
		return new EventFieldIndexingImpl(Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap());
	}
	
	@Override
	public void onNewEvent(long eventId, EventSerializer ed, Object event) {
		for (EventFieldIndex efi : eventFieldIndicies.values()) {
			if (efi.indexes(ed.getEventTypeId())) {
				efi.onNewEvent(eventId, event);
			}
		}
	}

	@Override
	public boolean isIndexing(EventFieldId eventFieldId) {
		return eventFieldIndicies.containsKey(eventFieldId);
	}

	@Override
	public Iterable<EventId> getIterable(EventFieldId eventFieldId, FieldConstraint fieldConstraint) {
		return eventFieldIndicies.get(eventFieldId).getIterable(fieldConstraint);
	}

	@Override
	public void stop() {
		for (EventFieldIndex efi : eventFieldIndicies.values()) {
			efi.close();
		}
	}

	@Override
	public String toString() {
		return "EventFieldIndexingImpl [eventFieldIndicies=" + eventFieldIndicies.keySet() + "]";
	}

}
