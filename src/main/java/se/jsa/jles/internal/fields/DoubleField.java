package se.jsa.jles.internal.fields;

import java.nio.ByteBuffer;

public class DoubleField extends EventField {

	public DoubleField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
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

}
