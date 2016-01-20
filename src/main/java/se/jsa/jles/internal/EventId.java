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
package se.jsa.jles.internal;

public class EventId {

	private final long eventId;

	public EventId(long eventId) {
		this.eventId = eventId;
	}

	public long toLong() {
		return eventId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof EventId)) {
			return false;
		}
		EventId other = (EventId)obj;
		return eventId == other.eventId;
	}

	@Override
	public int hashCode() {
		return (int) eventId;
	}

	@Override
	public String toString() {
		return "EventId [eventId=" + eventId + "]";
	}


}
