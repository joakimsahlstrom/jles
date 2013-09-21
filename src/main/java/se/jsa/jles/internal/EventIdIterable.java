package se.jsa.jles.internal;

import java.util.Iterator;

import se.jsa.jles.internal.IndexFile.IndexEntry;
import se.jsa.jles.internal.util.Objects;

public class EventIdIterable<T> implements Iterable<EventId> {
	private final Iterable<IndexEntry<T>> indexSource;

	public EventIdIterable(Iterable<IndexEntry<T>> indexSource) {
		this.indexSource = Objects.requireNonNull(indexSource);
	}

	@Override
	public Iterator<EventId> iterator() {
		return new EventIndexIterator<T>(indexSource.iterator());
	}

	public static class EventIndexIterator<T> implements Iterator<EventId> {
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
		public EventId next() {
			return new EventId(indexSource.next().getEventIndex(), eventIdByType++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
