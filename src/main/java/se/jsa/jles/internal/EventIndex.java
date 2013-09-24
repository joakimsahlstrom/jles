package se.jsa.jles.internal;

import java.nio.ByteBuffer;

import se.jsa.jles.internal.IndexFile.IndexEntry;
import se.jsa.jles.internal.fields.StorableField;

/**
 * A very simple kind of index only indexing where in the event repo file events
 * of a specific type exists
 *
 * @author joakim
 *
 */
public class EventIndex {

	private final IndexFile indexFile;

	public EventIndex(EntryFile entryFile, Long eventTypeId) {
		this.indexFile = new IndexFile(new NullEventTypeField(eventTypeId), entryFile);
	}

	public Iterable<IndexEntry<Long>> readIndicies() {
		return indexFile.readIndicies(Long.class);
	}

	public void writeIndex(long eventId) {
		indexFile.writeIndex(eventId, null);
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
	}

	public void close() {
		indexFile.close();
	}

}
