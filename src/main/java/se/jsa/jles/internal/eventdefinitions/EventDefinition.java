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
package se.jsa.jles.internal.eventdefinitions;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.FieldSerializer;
import se.jsa.jles.internal.util.NullFieldMap;
import se.jsa.jles.internal.util.Objects;

class EventDefinition implements EventSerializer, EventDeserializer {
	private final EventTypeId id;
	private final Class<?> eventType;
	private final List<EventField> fields;

	EventDefinition(EventTypeId id, Class<?> eventType, List<EventField> fields) {
		this.id = Objects.requireNonNull(id);
		this.eventType = Objects.requireNonNull(eventType);
		this.fields = Collections.unmodifiableList(new ArrayList<EventField>(fields));
	}

	@Override
	public EventTypeId getEventTypeId() {
		return id;
	}

	Class<?> getEventType() {
		return eventType;
	}

	ByteBuffer toEventFileEntry(FieldSerializer fieldSerializer) {
		int eventDefinitionLength = fieldSerializer.getSerializedStringLength(getEventType().getName()) + 4;
		for (EventField ef : getFields()) {
			eventDefinitionLength += ef.getDefinitionLength(fieldSerializer);
		}

		ByteBuffer output = ByteBuffer.allocate(8 + 4 + eventDefinitionLength);
		output.putLong(getEventTypeId().toLong()); 						// eventTypeId
		output.putInt(eventDefinitionLength);							// eventDefinitionSize
		fieldSerializer.putString(output, getEventType().getName());    // eventDefinition
		output.putInt(getFields().size());
		for (EventField ef : getFields()) {
			ef.writeDefinition(output, fieldSerializer);
		}
		return output;
	}

	@Override
	public ByteBuffer serializeEvent(Object event) {
		NullFieldMap nullFieldMap = NullFieldMap.buildFromEvent(fields, event);
		int totalSize = calculateSize(event, nullFieldMap);

		ByteBuffer result = ByteBuffer.allocate(totalSize);
		result.put(nullFieldMap.getMask());
		for (EventField pf : fields) {
			if (!nullFieldMap.isFieldNull(pf)) {
				pf.writeToBuffer(event, result);
			}
		}
		result.rewind();
		return result;
	}

	@Override
	public Object deserializeEvent(ByteBuffer buffer) {
		verifyEventTypeId(buffer.getLong());
		buffer.position(12);

		Object instance = createEventInstance();
		NullFieldMap nullFieldMap = readNullFieldMap(buffer);
		for (EventField pf : fields) {
			try {
				if (!nullFieldMap.isFieldNull(pf)) {
					pf.readFromBuffer(instance, buffer);
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"Could not deserialize type: " + eventType 
						+ ". Field: " + pf
						+ ". Buffer.remaining: " + buffer.remaining(), e);
			}
		}
		return getEventType().cast(instance);
	}
	
	private Object createEventInstance() {
		try {
			Constructor<?> constructor = eventType.getConstructor();
			return constructor.newInstance();
		} catch (Exception e1) {
			throw new RuntimeException(
					"Could not create event type: " + eventType 
					+ ". Found constructors: " + Arrays.asList(eventType.getConstructors()));
		}
	}

	private NullFieldMap readNullFieldMap(ByteBuffer buffer) {
		try {
			return NullFieldMap.buildFromBuffer(fields, buffer);
		} catch (Exception e) {
			throw new RuntimeException(
					"Could not read null field map for: " + eventType 
					+ ". Fields: " + fields
					+ ". Buffer: " + buffer, e);
		}
	}

	@Override
	public Object deserializeEventField(ByteBuffer buffer, EventField eventField) {
		verifyEventTypeId(buffer.getLong());
		buffer.position(12);

		try {
			NullFieldMap nullFieldMap = NullFieldMap.buildFromBuffer(fields, buffer);
			for (EventField pf : fields) {
				if (!nullFieldMap.isFieldNull(pf)) {
					if (pf.equals(eventField)) {
						return pf.readFromBuffer(buffer);
					} else {
						pf.readFromBuffer(buffer);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not deserialize type: " + eventType + ". Found constructors: " + Arrays.asList(eventType.getConstructors()), e);
		}
		return null; // field not found above => is null
	}

	public EventField getField(String fieldName) {
		for (EventField ef : fields) {
			if (ef.matchesFieldName(fieldName)) {
				return ef;
			}
		}
		throw new IllegalArgumentException("Field with name " + fieldName + " does not exist for event type " + eventType.getSimpleName() + ". Available fields: " + fields);
	}

	List<EventField> getFields() {
		return fields;
	}

	private void verifyEventTypeId(Long id) {
		if (this.id.toLong() != id.longValue()) {
			throw new IllegalArgumentException("Invalid event type. Expected " + this.id + " got " + id);
		}
	}

	private int calculateSize(Object event, NullFieldMap nullFieldMap) {
		int size = NullFieldMap.getSizeInBytes(fields);
		for (EventField ef : fields) {
			if (!nullFieldMap.isFieldNull(ef)) {
				size += ef.getSize(event);
			}
		}
		return size;
	}

	@Override
	public int hashCode() {
		return eventType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventDefinition) {
			EventDefinition other = (EventDefinition)obj;
			return this.eventType.equals(other.eventType)
					&& this.id.equals(other.id)
					&& this.fields.equals(other.fields);
		}
		return false;
	}

	@Override
	public String toString() {
		return "EventDefinition [id=" + id + ", eventType=" + eventType.getSimpleName() + ", fields=" + fields + "]";
	}

}
