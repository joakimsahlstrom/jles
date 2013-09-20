package se.jsa.jles.internal;

import java.nio.ByteBuffer;

public interface EventSerializer {

	ByteBuffer serializeEvent(Object event);

	long getEventTypeId();

}
