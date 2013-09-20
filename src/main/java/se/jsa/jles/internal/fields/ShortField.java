package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class ShortField extends EventField {

	ShortField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	ShortField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	@Override
	public Class<?> getFieldType() {
		return Short.TYPE;
	}

	@Override
	public int getSize(Object event) {
		return 2;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putShort(Short.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getShort();
	}

	@Override
	public boolean isOfType(Class<?> type) {
		return Short.TYPE.equals(type) || Short.class.equals(type);
	}

}
