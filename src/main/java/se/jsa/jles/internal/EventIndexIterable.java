package se.jsa.jles.internal;

import java.util.Iterator;

import se.jsa.jles.internal.IndexFile.IndexEntry;
import se.jsa.jles.internal.util.Objects;

public class EventIndexIterable<T> implements Iterable<EventIndex> {
	private final Iterable<IndexEntry<T>> indexSource;

	public EventIndexIterable(Iterable<IndexEntry<T>> indexSource) {
		this.indexSource = Objects.requireNonNull(indexSource);
	}

	@Override
	public Iterator<EventIndex> iterator() {
		return new EventIndexIterator<T>(indexSource.iterator());
	}

	public static class EventIndexIterator<T> implements Iterator<EventIndex> {
		private final Iterator<IndexEntry<T>> indexSource;
		private long eventIdByType = 0;

		public EventIndexIterator(Iterator<IndexEntry<T>> indexSource) {
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
