package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class LongField extends EventField {

	public LongField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	public LongField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	@Override
	public Class<?> getFieldType() {
		return Long.TYPE;
	}

	@Override
	public int getSize(Object event) {
		return 8;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putLong(Long.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getLong();
	}

	@Override
	public boolean isOfType(Class<?> type) {
		return Long.TYPE.equals(type) || Long.class.equals(type);
	}

}
