package se.jsa.jles.internal.eventdefinitions;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.FieldSerializer;
import se.jsa.jles.internal.util.Objects;

class EventDefinition implements EventSerializer, EventDeserializer {
	private final Long id;
	private final Class<?> eventType;
	private final List<EventField> fields;

	EventDefinition(Long id, Class<?> eventType, List<EventField> fields) {
		this.id = Objects.requireNonNull(id);
		this.eventType = Objects.requireNonNull(eventType);
		this.fields = Collections.unmodifiableList(new ArrayList<EventField>(fields));
	}

	@Override
	public long getEventTypeId() {
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
		output.putLong(getEventTypeId()); 								// eventTypeId
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
		int totalSize = calculateSize(event);
		ByteBuffer result = ByteBuffer.allocate(totalSize);
		for (EventField pf : fields) {
			pf.writeToBuffer(event, result);
		}
		result.rewind();
		return result;
	}

	@Override
	public Object deserializeEvent(ByteBuffer buffer) {
		verifyEventTypeId(buffer.getLong());
		buffer.position(12);

		Object instance;
		try {
			Constructor<?> constructor = eventType.getConstructor();
			instance = constructor.newInstance();
			for (EventField pf : fields) {
				pf.readFromBuffer(instance, buffer);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not deserialize type: " + eventType + ". Found constructors: " + Arrays.asList(eventType.getConstructors()), e);
		}
		return getEventType().cast(instance);
	}

	public EventField getField(String fieldName) {
		for (EventField ef : fields) {
			if (ef.getPropertyName().equals(fieldName)) {
				return ef;
			}
		}
		throw new IllegalArgumentException("Field with name " + fieldName + " does not exist for event type " + eventType.getSimpleName());
	}

	List<EventField> getFields() {
		return fields;
	}

	private void verifyEventTypeId(Long id) {
		if (!this.id.equals(id)) {
			throw new IllegalArgumentException("Invalid event type. Expected " + this.id + " got " + id);
		}
	}

	private int calculateSize(Object event) {
		int size = 0;
		for (EventField pf : fields) {
			size += pf.getSize(event);
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
		return "EventDefinition [id=" + id + ", eventType=" + eventType + ", fields=" + fields + "]";
	}

}