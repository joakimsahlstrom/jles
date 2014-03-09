package se.jsa.jles.internal;

import java.nio.ByteBuffer;

import se.jsa.jles.internal.fields.EventField;

public interface EventDeserializer {

	Object deserializeEvent(ByteBuffer input);
	Object deserializeEventField(ByteBuffer input, EventField eventField);

}
