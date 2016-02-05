/*
 * Copyright 2016 Joakim Sahlström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jsa.jles.internal;

import java.nio.ByteBuffer;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.internal.fields.EventField;

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
		output.putLong(ed.getEventTypeId().toLong());
		output.putInt(eventData.limit());
		if (eventData.limit() > 0) {
			output.put(eventData);
		}

		long eventPosition = entryFile.append(output);
		return eventPosition;
	}

	public Object readEvent(long position, EventDeserializer ed) {
		ByteBuffer input = entryFile.readEntry(position);
		
		try {
			Object result = ed.deserializeEvent(input);
			return result;
		} catch (RuntimeException e) {
			throw new RuntimeException("Could not deserialize event. Position=" + position + " deserializer=" + ed + " entryFile.size=" + entryFile.size(), e);
		}
	}

	public Object readEventField(long position, EventDeserializer ed, EventField eventField) {
		ByteBuffer input = entryFile.readEntry(position);

		Object result = ed.deserializeEventField(input, eventField);
		return result;
	}

	public void close() {
		entryFile.close();
	}

	public EventRepoReport report() {
		return new EventRepoReport()
			.appendLine(EventFile.class.getSimpleName() + " size: " + entryFile.size() + " bytes");
	}

}
