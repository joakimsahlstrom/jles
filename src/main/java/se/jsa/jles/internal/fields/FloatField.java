package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class FloatField extends EventField {

	public FloatField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	public FloatField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	@Override
	public Class<?> getFieldType() {
		return Float.TYPE;
	}

	@Override
	public int getSize(Object event) {
		return 4;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putFloat(Float.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getFloat();
	}

	@Override
	public boolean isOfType(Class<?> type) {
		return Float.TYPE.equals(type) || Float.class.equals(type);
	}

}
