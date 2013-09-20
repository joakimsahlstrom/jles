package se.jsa.jles.internal;

import java.nio.ByteBuffer;

public interface EventDeserializer {

	Object deserializeEvent(ByteBuffer input);

}
