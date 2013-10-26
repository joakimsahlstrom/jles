package se.jsa.jles.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.util.Objects;


public class LoadingIterable implements Iterable<Object> {
	private final List<EventIndexHolder> holders = new ArrayList<EventIndexHolder>();

	@Override
	public Iterator<Object> iterator() {
		return new LoadingIterator(createFeeders(holders));
	}

	private Collection<EventIndexFeeder> createFeeders(List<EventIndexHolder> holders) {
		ArrayList<EventIndexFeeder> result = new ArrayList<EventIndexFeeder>();
		for (EventIndexHolder holder : holders) {
			result.add(holder.toFeeder());
		}
		return result;
	}

	public void register(Iterable<EventId> iterable, TypedEventRepo eventRepo) {
		holders.add(new EventIndexHolder(iterable, eventRepo));
	}

	private class EventIndexHolder {
		private final Iterable<EventId> iterable;
		private final TypedEventRepo eventRepo;

		public EventIndexHolder(Iterable<EventId> iterable, TypedEventRepo eventRepo) {
			this.iterable = iterable;
			this.eventRepo = eventRepo;
		}

		public EventIndexFeeder toFeeder() {
			return new EventIndexFeeder(iterable.iterator(), eventRepo);
		}
	}

	private class EventIndexFeeder {
		private final Iterator<EventId> iterable;
		private final TypedEventRepo eventRepo;
		private EventId current = null;

		public EventIndexFeeder(Iterator<EventId> iterable, TypedEventRepo eventRepo) {
			this.iterable = iterable;
			this.eventRepo = eventRepo;
		}

		private boolean peek() {
			if (current == null && iterable.hasNext()) {
				current = iterable.next();
			}
			return current != null;
		}

		public Object take() {
			peek();
			EventId res = current;
			current = null;
			return eventRepo.readEvent(res);
		}

		public boolean before(EventIndexFeeder currentFeeder) {
			peek();
			if (current == null) {
				return false;
			}
			if (currentFeeder == null) {
				return true;
			}
			return current.getEventId() < currentFeeder.current.getEventId();
		}
	}


	private class LoadingIterator implements Iterator<Object> {
		private final Collection<EventIndexFeeder> feeders;
		private EventIndexFeeder currentFeeder;

		public LoadingIterator(Collection<EventIndexFeeder> feeders) {
			this.feeders = Objects.requireNonNull(feeders);
		}

		@Override
		public boolean hasNext() {
			if (currentFeeder == null) {
				for (EventIndexFeeder feeder : feeders) {
					if (feeder.before(currentFeeder)) {
						currentFeeder = feeder;
					}
				}
			}
			return currentFeeder != null;
		}

		@Override
		public Object next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Object result = currentFeeder.take();
			currentFeeder = null;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
