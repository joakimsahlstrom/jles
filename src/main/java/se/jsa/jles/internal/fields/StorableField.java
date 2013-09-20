package se.jsa.jles.internal.fields;

import java.nio.ByteBuffer;

public abstract class StorableField {

	public abstract Class<?> getFieldType();
	public abstract int getSize(Object event);
	public abstract void writeToBuffer(Object obj, ByteBuffer buffer);
	public abstract Object readFromBuffer(ByteBuffer buffer);

}
