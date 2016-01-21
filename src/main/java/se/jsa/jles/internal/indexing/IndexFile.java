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
package se.jsa.jles.internal.indexing;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventId;
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
				int size = entry.getInt();
				try {
					indexKey = readIndexKey(entry);
				} catch (Exception e) {
					throw new RuntimeException("Could not read index key. entryFile=" + entryFile
							+ ", position=" + position
							+ ", eventIndex=" + eventIndex
							+ ", size=" + size
							+ ", entry=" + entry,
							e);
				}
				result = new EventId(eventIndex);
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