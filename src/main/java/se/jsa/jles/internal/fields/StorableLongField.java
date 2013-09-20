package se.jsa.jles.internal.fields;

import java.nio.ByteBuffer;

public class StorableLongField extends StorableField {

	@Override
	public Class<?> getFieldType() {
		return Long.class;
	}

	@Override
	public int getSize(Object event) {
		return 8;
	}

	@Override
	public void writeToBuffer(Object obj, ByteBuffer buffer) {
		buffer.putLong(Long.class.cast(obj));
	}

	@Override
	public Long readFromBuffer(ByteBuffer buffer) {
		return buffer.getLong();
	}

}
