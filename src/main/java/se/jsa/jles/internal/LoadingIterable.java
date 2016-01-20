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
package se.jsa.jles.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.util.Objects;


public class LoadingIterable implements Iterable<Object> {
	private final List<EventIndexHolder> holders;

	private LoadingIterable(List<EventIndexHolder> holders) {
		this.holders = Collections.unmodifiableList(Objects.requireNonNull(holders));
	}

	public static LoadingIterable empty() {
		return new LoadingIterable(new ArrayList<EventIndexHolder>());
	}

	public LoadingIterable with(Iterable<EventId> iterable, TypedEventRepo eventRepo) {
		ArrayList<EventIndexHolder> result = new ArrayList<EventIndexHolder>(holders);
		result.add(new EventIndexHolder(iterable, eventRepo));
		return new LoadingIterable(result);
	}

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
			return current.toLong() < currentFeeder.current.toLong();
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
