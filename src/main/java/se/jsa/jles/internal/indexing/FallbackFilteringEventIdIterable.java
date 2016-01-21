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

import java.util.Iterator;
import java.util.NoSuchElementException;

import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.util.Objects;

public class FallbackFilteringEventIdIterable implements Iterable<EventId> {

	private final Iterable<EventId> baseIter;
	private final FieldConstraint constraint;
	private final TypedEventRepo typedEventRepo;

	public FallbackFilteringEventIdIterable(Iterable<EventId> baseIter, FieldConstraint constraint, TypedEventRepo typedEventRepo) {
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
		private final FieldConstraint constraint;
		private final TypedEventRepo typedEventRepo;

		private EventId next = null;

		public FallbackFilteringEventIdIterator(Iterator<EventId> iterator, FieldConstraint constraint, TypedEventRepo typedEventRepo) {
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
