package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class StringField extends EventField {

	public StringField(Method getMethod, Method setMethod) {
		super(getMethod, setMethod);
	}

	public StringField(Class<?> eventType, String value) {
		super(eventType, value);
	}

	@Override
	public Class<?> getFieldType() {
		return String.class;
	}

	@Override
	public int getSize(Object event) {
		return 4 + String.class.cast(getValue(event)).getBytes().length;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		String val = String.class.cast(getValue(event));
		byte[] stringData = val.getBytes();
		buffer.putInt(stringData.length);
		buffer.put(stringData);
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte[] stringData = new byte[length];
		buffer.get(stringData);

		return new String(stringData);
	}

	@Override
	public boolean isOfType(Class<?> type) {
		return String.class.equals(type);
	}

}
