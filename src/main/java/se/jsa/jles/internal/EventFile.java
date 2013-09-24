package se.jsa.jles.internal;

import java.nio.ByteBuffer;

/**
 * EventFile
 *
 * Record: eventType(8b):eventBodySize(4b)[:eventBody(eventBodySize)]
 *
 * @author joakim
 *
 */
public class EventFile {
	private final EntryFile entryFile;

	public EventFile(EntryFile entryFile) {
		this.entryFile = entryFile;
	}

	public long writeEvent(Object event, EventSerializer ed) {
		ByteBuffer eventData = ed.serializeEvent(event);

		ByteBuffer output = ByteBuffer.allocate(8 + 4 + eventData.limit());
		output.putLong(ed.getEventTypeId());
		output.putInt(eventData.limit());
		if (eventData.limit() > 0) {
			output.put(eventData);
		}

		long eventPosition = entryFile.append(output);
		return eventPosition;
	}

	public Object readEvent(long position, EventDeserializer ed) {
		ByteBuffer input = entryFile.readEntry(position);

		Object result = ed.deserializeEvent(input);
		return result;
	}

	public void close() {
		entryFile.close();
	}

}
