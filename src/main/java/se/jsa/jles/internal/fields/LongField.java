package se.jsa.jles.internal.fields;

import java.nio.ByteBuffer;

public class LongField extends EventField {

	public LongField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
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

}
