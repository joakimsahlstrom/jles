package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class ByteField extends EventField {

	public ByteField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	public ByteField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	@Override
	public Class<?> getFieldType() {
		return Byte.TYPE;
	}

	@Override
	public int getSize(Object event) {
		return 1;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.put(Byte.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.get();
	}

	@Override
	public boolean isOfType(Class<?> type) {
		return Byte.TYPE.equals(type) || Byte.class.equals(type);
	}

}
