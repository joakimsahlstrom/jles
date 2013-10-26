package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class BooleanField extends EventField {

	public BooleanField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	public BooleanField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	@Override
	public int getSize(Object event) {
		return 1;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.put((byte) (Boolean.class.cast(getValue(event)) ? 1 : 0));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.get() == 1 ? true : false;
	}

}
