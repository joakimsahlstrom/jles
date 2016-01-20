package se.jsa.jles.internal.fields;

import java.nio.ByteBuffer;

public class ByteField extends EventField {

	public ByteField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
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

}
