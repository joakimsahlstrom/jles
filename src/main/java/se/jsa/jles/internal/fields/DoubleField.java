package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class DoubleField extends EventField {

	public DoubleField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	public DoubleField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	@Override
	public Class<?> getFieldType() {
		return Double.TYPE;
	}

	@Override
	public int getSize(Object event) {
		return 8;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putDouble(Double.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getDouble();
	}

	@Override
	public boolean isOfType(Class<?> type) {
		return Double.TYPE.equals(type) || Double.class.equals(type);
	}

}
