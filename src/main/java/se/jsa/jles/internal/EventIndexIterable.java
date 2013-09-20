package se.jsa.jles.internal;

import java.util.Iterator;

import se.jsa.jles.internal.IndexFile.IndexEntry;
import se.jsa.jles.internal.util.Objects;

public class EventIndexIterable implements Iterable<EventIndex> {
	private final Iterable<IndexEntry<Long>> indexSource;

	public EventIndexIterable(Iterable<IndexEntry<Long>> indexSource) {
		this.indexSource = Objects.requireNonNull(indexSource);
	}

	@Override
	public Iterator<EventIndex> iterator() {
		return new EventIndexIterator(indexSource.iterator());
	}

	public static class EventIndexIterator implements Iterator<EventIndex> {
		private final Iterator<IndexEntry<Long>> indexSource;
		private long eventIdByType = 0;

		public EventIndexIterator(Iterator<IndexEntry<Long>> indexSource) {
			this.indexSource = Objects.requireNonNull(indexSource);
		}

		@Override
		public boolean hasNext() {
			return indexSource.hasNext();
		}

		@Override
		public EventIndex next() {
			return new EventIndex(indexSource.next().getEventIndex(), eventIdByType++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
