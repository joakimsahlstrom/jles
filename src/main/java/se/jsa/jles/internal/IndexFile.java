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

	public interface IndexKeyMatcher<T> {
		boolean accepts(T t);
	}

	public class IndexEntry<T> {
		private final long eventIndex;
		private final T key;

		IndexEntry(long eventIndex, T key) {
			this.eventIndex = eventIndex;
			this.key = key;
		}

		public long getEventIndex() {
			return eventIndex;
		}

		public T getKey() {
			return key;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IndexEntry) {
				@SuppressWarnings("unchecked")
				IndexEntry<T> other = (IndexEntry<T>)obj;
				return eventIndex == other.eventIndex
						&& key.equals(other.key);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (int) (eventIndex * 31 + key.hashCode());
		}

		@Override
		public String toString() {
			return "IndexEntry [eventIndex=" + eventIndex + ", key=" + key
					+ "]";
		}
	}

	private static IndexKeyMatcher<Object> ALWAYS_MATCHER = new IndexKeyMatcher<Object>() {
		@Override
		public boolean accepts(Object t) {
			return true;
		}
	};

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

	@SuppressWarnings("unchecked")
	public <T> Iterable<IndexEntry<T>> readIndicies(Class<T> keyType) {
		return readIndicies(keyType, (IndexKeyMatcher<T>)ALWAYS_MATCHER);
	}

	public <T> Iterable<IndexEntry<T>> readIndicies(Class<T> keyType, IndexKeyMatcher<T> matcher) {
		return new IndexIterable<T>(keyType, matcher);
	}

	private class IndexIterable<T> implements Iterable<IndexEntry<T>> {
		private final Class<T> keyType;
		private final IndexKeyMatcher<T> matcher;
		public IndexIterable(Class<T> keyType, IndexKeyMatcher<T> matcher) {
			this.keyType = keyType;
			this.matcher = matcher;
		}
		@Override
		public Iterator<IndexEntry<T>> iterator() {
			return new IndexIterator<T>(keyType, matcher, entryFile.size());
		}
	}

	private class IndexIterator<T> implements Iterator<IndexEntry<T>> {
		private final Class<T> keyType;
		private final IndexKeyMatcher<T> matcher;
		private final long fileSize;

		private long position = 0;
		private IndexEntry<T> nextEntry = null;

		public IndexIterator(Class<T> keyType, IndexKeyMatcher<T> matcher, long fileSize) {
			this.keyType = keyType;
			this.matcher = matcher;
			this.fileSize = fileSize;
		}

		@Override
		public IndexEntry<T> next() {
			if (hasNext()) {
				IndexEntry<T> result = nextEntry;
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
			T indexKey;
			do {
				entry = entryFile.readEntry(position);
				eventIndex = entry.getLong();
				entry.getInt(); // size
				indexKey = keyType.cast(eventKeyField.readFromBuffer(entry));
			} while (!matcher.accepts(indexKey) && (position += entry.limit()) < fileSize);

			if (position < fileSize) {
				nextEntry = new IndexEntry<T>(eventIndex, indexKey);
				position += entry.limit();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Remove not supported.");
		}

	}

}
