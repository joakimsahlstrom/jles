package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class IntegerField extends EventField {

	public IntegerField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	public IntegerField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	@Override
	public int getSize(Object event) {
		return 4;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putInt(Integer.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getInt();
	}

}
