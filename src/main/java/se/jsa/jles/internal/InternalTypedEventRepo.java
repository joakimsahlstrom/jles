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

import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class InternalTypedEventRepo implements TypedEventRepo {
	private final EventTypeId eventTypeId;
	private final EventDeserializer eventDeserializer;
	private final EventFile eventFile;
	private final EventDefinitions eventDefinitions;

	public InternalTypedEventRepo(EventTypeId eventTypeId, EventFile eventFile, EventDefinitions eventDefinitions) {
		this.eventTypeId = Objects.requireNonNull(eventTypeId);
		this.eventFile = eventFile;
		this.eventDefinitions = eventDefinitions;
		this.eventDeserializer = eventDefinitions.getEventDeserializer(eventTypeId);
	}

	@Override
	public Object readEvent(EventId eventIndex) {
		return eventFile.readEvent(eventIndex.toLong(), eventDeserializer);
	}

	@Override
	public Object readEventField(EventId eventId, String fieldName) {
		EventField eventField = eventDefinitions.getEventField(eventTypeId, fieldName);
		return eventFile.readEventField(eventId.toLong(), eventDeserializer, eventField);
	}
}