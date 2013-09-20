package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public abstract class EventField extends StorableField {
	private final Method getMethod;
	private final Method setMethod;
	private final String propertyName;

	EventField(Class<?> eventType, String propertyName) {
		this.propertyName = propertyName;
		try {
			getMethod = eventType.getMethod("get" + propertyName);
			setMethod = eventType.getMethod("set" + propertyName, getFieldType());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	EventField(Method getMethod, Method setMethod) {
		this.getMethod = getMethod;
		this.setMethod = setMethod;
		this.propertyName = getMethod.getName().substring(3);
	}

	public Object getValue(Object event) {
		try {
			return getMethod.invoke(event);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void setValue(Object event, Object value) {
		try {
			setMethod.invoke(event, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void readFromBuffer(Object event, ByteBuffer buffer) {
		setValue(event, readFromBuffer(buffer));
	}

	public int getDefinitionLength(FieldSerializer fieldSerializer) {
		return fieldSerializer.getSerializedStringLength(getFieldType().getName())
				+ fieldSerializer.getSerializedStringLength(getMethod.getName().substring(3));
	}

	public void writeDefinition(ByteBuffer output, FieldSerializer fieldSerializer) {
		fieldSerializer.putString(output, getFieldType().getName());
		fieldSerializer.putString(output, getMethod.getName().substring(3));
	}

	public String getPropertyName() {
		return propertyName;
	}

	public abstract boolean isOfType(Class<?> type);

	@Override
	public int hashCode() {
		return getMethod.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventField) {
			EventField other = (EventField) obj;
			return getMethod.equals(other.getMethod)
					&& setMethod.equals(other.setMethod);
		}
		return false;
	}

	@Override
	public String toString() {
		return getMethod.getDeclaringClass().getSimpleName() + "." + getMethod.getName().substring(3) + "[" + getMethod.getReturnType().getSimpleName() + "]";
	}

}
