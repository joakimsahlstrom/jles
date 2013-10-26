package se.jsa.jles.internal;

import java.nio.ByteBuffer;

import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.fields.StorableField;
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
	private final Long eventTypeId;

	public EventIndex(EntryFile entryFile, Long eventTypeId) {
		this.indexFile = new IndexFile(new NullEventTypeField(eventTypeId), entryFile);
		this.eventTypeId = Objects.requireNonNull(eventTypeId);
	}

	public static IndexKeyMatcher ALWAYS_MATCHER = new IndexKeyMatcher() {
		@Override
		public boolean accepts(Object t) {
			return true;
		}
	};

	public Iterable<EventId> readIndicies() {
		return indexFile.readIndicies(ALWAYS_MATCHER);
	}

	public void writeIndex(long eventId) {
		indexFile.writeIndex(eventId, null);
	}

	public Long getEventTypeId() {
		return eventTypeId;
	}

	private static class NullEventTypeField extends StorableField {
		private final Long eventTypeId;

		public NullEventTypeField(Long eventTypeId) {
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
