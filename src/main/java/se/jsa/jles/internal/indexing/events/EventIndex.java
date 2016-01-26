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
package se.jsa.jles.internal.indexing.events;

import java.nio.ByteBuffer;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.StorableField;
import se.jsa.jles.internal.indexing.IndexFile;
import se.jsa.jles.internal.indexing.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.util.Objects;

/**
 * A very simple kind of index only indexing where in the event repo file events
 * of a specific type exists
 *
 * @author joakim
 *
 */
public class EventIndex {
	private final IndexFile indexFile;
	private final EventTypeId eventTypeId;

	public EventIndex(EntryFile entryFile, EventTypeId eventTypeId) {
		this.indexFile = new IndexFile(new NullEventTypeField(eventTypeId), entryFile);
		this.eventTypeId = Objects.requireNonNull(eventTypeId);
	}

	public static IndexKeyMatcher ALWAYS_MATCHER = new IndexKeyMatcher() {
		@Override
		public boolean accepts(Object t) {
			return true;
		}
	};

	public EventRepoReport report() {
		return indexFile.report();
	}

	public Iterable<EventId> readIndicies() {
		return indexFile.readIndicies(ALWAYS_MATCHER);
	}

	public void writeIndex(long eventId) {
		indexFile.writeIndex(eventId, null);
	}

	public EventTypeId getEventTypeId() {
		return eventTypeId;
	}

	private static class NullEventTypeField extends StorableField {
		private final EventTypeId eventTypeId;

		public NullEventTypeField(EventTypeId eventTypeId) {
			this.eventTypeId = eventTypeId;
		}

		@Override
		public Class<Long> getFieldType() {
			return Long.class;
		}

		@Override
		public int getSize(Object event) {
			return 0;
		}

		@Override
		public void writeToBuffer(Object obj, ByteBuffer buffer) {
			// do nothing
		}

		@Override
		public Object readFromBuffer(ByteBuffer buffer) {
			return eventTypeId;
		}

		@Override
		public boolean isNullable() {
			return false;
		}

		@Override
		public boolean isNull(Object event) {
			return false;
		}
	}

	public void close() {
		indexFile.close();
	}

}
