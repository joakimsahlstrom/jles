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
		int eventKeySize = eventKeyField.getSize(event);
		ByteBuffer output = ByteBuffer.allocate(8 + 4 + eventKeySize);
		output.putLong(eventId);
		output.putInt(eventKeySize);
		eventKeyField.writeToBuffer(event, output);
		output.rewind();

		entryFile.append(output);
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
			return new IndexIterator(matcher, entryFile.size());
		}
	}

	private class IndexIterator implements Iterator<EventId> {
		private final IndexKeyMatcher matcher;
		private final long fileSize;

		private long position = 0;
		private long eventIdByType = 0;
		private EventId nextEntry = null;

		public IndexIterator(IndexKeyMatcher matcher, long fileSize) {
			this.matcher = matcher;
			this.fileSize = fileSize;
		}

		@Override
		public EventId next() {
			if (hasNext()) {
				EventId result = nextEntry;
				nextEntry = null;
				return result;
			} else {
				throw new NoSuchElementException("End of index file.");
			}
		}

		@Override
		public boolean hasNext() {
			if (nextEntry != null) {
				return true;
			}
			if (position >= fileSize) {
				return false;
			}

			tryLoadNextEntry();
			return nextEntry != null;
		}

		private void tryLoadNextEntry() {
			ByteBuffer entry;
			long eventIndex;
			Object indexKey;
			EventId result;
			do {
				entry = entryFile.readEntry(position);
				eventIndex = entry.getLong();
				entry.getInt(); // size
				indexKey = eventKeyField.readFromBuffer(entry);
				result = new EventId(eventIndex, eventIdByType++);
			} while (!matcher.accepts(indexKey) && (position += entry.limit()) < fileSize);

			if (position < fileSize) {
				nextEntry = result;
				position += entry.limit();
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
