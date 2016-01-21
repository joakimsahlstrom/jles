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

import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;


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
