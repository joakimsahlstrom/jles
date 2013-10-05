package se.jsa.jles.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.util.Objects;


public class FallbackFilteringEventIdIterable implements Iterable<EventId> {

	private final Iterable<EventId> baseIter;
	private final EventFieldConstraint constraint;
	private final TypedEventRepo typedEventRepo;

	public FallbackFilteringEventIdIterable(Iterable<EventId> baseIter, EventFieldConstraint constraint, TypedEventRepo typedEventRepo) {
		this.baseIter = Objects.requireNonNull(baseIter);
		this.constraint = Objects.requireNonNull(constraint);
		this.typedEventRepo = Objects.requireNonNull(typedEventRepo);
	}

	@Override
	public Iterator<EventId> iterator() {
		return new FallbackFilteringEventIdIterator(baseIter.iterator(), constraint, typedEventRepo);
	}

	private static class FallbackFilteringEventIdIterator implements Iterator<EventId> {
		private final Iterator<EventId> iterator;
		private final EventFieldConstraint constraint;
		private final TypedEventRepo typedEventRepo;

		private EventId next = null;

		public FallbackFilteringEventIdIterator(Iterator<EventId> iterator, EventFieldConstraint constraint, TypedEventRepo typedEventRepo) {
			this.iterator = Objects.requireNonNull(iterator);
			this.constraint = Objects.requireNonNull(constraint);
			this.typedEventRepo = Objects.requireNonNull(typedEventRepo);
		}

		@Override
		public boolean hasNext() {
			while (next == null && iterator.hasNext()) {
				EventId eventId = iterator.next();
				if (constraint.accepts(typedEventRepo.readEventField(eventId, constraint.getFieldName()))) {
					next = eventId;
				}
			}
			return next != null;
		}

		@Override
		public EventId next() {
			if (next == null) {
				throw new NoSuchElementException();
			}
			EventId eId = next;
			next = null;
			return eId;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
