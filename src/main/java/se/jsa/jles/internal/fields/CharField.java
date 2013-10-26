package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class CharField extends EventField {

	public CharField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	public CharField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	@Override
	public int getSize(Object event) {
		return 2;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putChar(Character.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getChar();
	}

}
