package se.jsa.jles.internal;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.fields.StorableField;

/**
 * IndexFile
 *
 * eventIndex(8b):eventKeySize(4b):eventKey(eventKeySize)
 *
 * @author joakim
 *
 */
public class IndexFile {

	public interface IndexKeyMatcher {
		boolean accepts(Object o);
	}

	final StorableField eventKeyField;
	final EntryFile entryFile;

	public IndexFile(StorableField eventKeyField, EntryFile entryFile) {
		this.eventKeyField = eventKeyField;
		this.entryFile = entryFile;
	}

	public void writeIndex(long eventId, Object event) {
		ByteBuffer output;
		if (eventKeyField.isNullable()) {
			output = writeNullableIndex(eventId, event);
		} else {
			output = writeNonNullableIndex(eventId, event);
		}
		entryFile.append(output);
	}

	private ByteBuffer writeNullableIndex(long eventId, Object event) {
		if (eventKeyField.isNull(event)) {
			return writeNullField(eventId);
		} else {
			int eventKeySize = eventKeyField.getSize(event) + 1;
			ByteBuffer output = ByteBuffer.allocate(8 + 4 + eventKeySize);
			output.putLong(eventId);
			output.putInt(eventKeySize);
			output.put((byte) 1);
			eventKeyField.writeToBuffer(event, output);
			output.rewind();
			return output;
		}
	}

	private ByteBuffer writeNullField(long eventId) {
		int eventKeySize = eventKeyField.getSize(1);
		ByteBuffer output = ByteBuffer.allocate(8 + 4 + eventKeySize);
		output.putLong(eventId);
		output.putInt(eventKeySize);
		output.put((byte) 0);
		output.rewind();
		return output;
	}

	private ByteBuffer writeNonNullableIndex(long eventId, Object event) {
		int eventKeySize = eventKeyField.getSize(event);
		ByteBuffer output = ByteBuffer.allocate(8 + 4 + eventKeySize);
		output.putLong(eventId);
		output.putInt(eventKeySize);
		eventKeyField.writeToBuffer(event, output);
		output.rewind();
		return output;
	}

	public Iterable<EventId> readIndicies(IndexKeyMatcher matcher) {
		return new IndexIterable(matcher);
	}

	private class IndexIterable implements Iterable<EventId> {
		private final IndexKeyMatcher matcher;
		public IndexIterable(IndexKeyMatcher matcher) {
			this.matcher = matcher;
		}
		@Override
		public Iterator<EventId> iterator() {
			return new IndexIterator(matcher);
		}
	}

	private class IndexIterator implements Iterator<EventId> {
		private final IndexKeyMatcher matcher;

		private long position = 0;
		private long eventIdByType = 0;
		private EventId nextEntry = null;
		private boolean nextEntryReady = false;

		public IndexIterator(IndexKeyMatcher matcher) {
			this.matcher = matcher;
		}

		@Override
		public EventId next() {
			if (hasNext()) {
				EventId result = nextEntry;
				nextEntry = null;
				nextEntryReady = false;
				return result;
			} else {
				throw new NoSuchElementException("End of index file.");
			}
		}

		@Override
		public boolean hasNext() {
			if (nextEntryReady) {
				return true;
			}
			if (position >= entryFile.size()) {
				return false;
			}

			tryLoadNextEntry();
			return nextEntryReady;
		}

		private void tryLoadNextEntry() {
			ByteBuffer entry;
			long eventIndex;
			Object indexKey;
			EventId result;
			long fileSize = entryFile.size();
			do {
				entry = entryFile.readEntry(position);
				eventIndex = entry.getLong();
				entry.getInt(); // size
				indexKey = readIndexKey(entry);
				result = new EventId(eventIndex, eventIdByType++);
			} while (!matcher.accepts(indexKey) && (position += entry.limit()) < fileSize);

			if (position < fileSize) {
				nextEntry = result;
				position += entry.limit();
				nextEntryReady = true;
			}
		}

		private Object readIndexKey(ByteBuffer entry) {
			if (eventKeyField.isNullable()) {
				return readNullableIndexKey(entry);
			} else {
				return eventKeyField.readFromBuffer(entry);
			}
		}

		private Object readNullableIndexKey(ByteBuffer entry) {
			if (entry.get() == (byte) 0) {
				return null;
			} else {
				return eventKeyField.readFromBuffer(entry);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported.");
		}
	}

	public void close() {
		entryFile.close();
	}

	@Override
	public String toString() {
		return "IndexFile [eventKeyField=" + eventKeyField + ", entryFile=" + entryFile + "]";
	}

}
