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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.EventStore;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.indexing.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.indexing.events.EventIndex;
import se.jsa.jles.internal.indexing.events.EventIndexing;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.indexing.fields.EventFieldIndexing;
import se.jsa.jles.internal.util.Objects;

/**
 * The class responsible for maintaining the overall indexing of jles
 * @author joakim Joakim Sahlström
 *
 */
public class Indexing {
	private final EventIndexing eventIndexing;
	private final EventFieldIndexing eventFieldIndexing;
	private final IndexUpdater indexUpdater;

	/**
	 * @param eventTypeIndexFile The {@link IndexFile} containing the main mapping data between event file indexes and the event types
	 * @param eventIndicies A Map with the {@link EventIndex}es that should exist for corresponding event type (ids)
	 * @param eventFieldIndicies A Map with the {@link EventFieldIndex}es that should exist for corresponding event fields
	 * @param multiThreadedEnvironment <code>true</code> if this {@link Indexing} is living in a multithreaded environment (and thus must be thread safe)
	 */
	public Indexing(EventIndexing eventIndexing, EventFieldIndexing eventFieldIndicies, boolean multiThreadedEnvironment) {
		this.eventIndexing = Objects.requireNonNull(eventIndexing);
		this.eventFieldIndexing = Objects.requireNonNull(eventFieldIndicies);
		this.indexUpdater = createIndexUpdater(eventIndexing, eventFieldIndicies, multiThreadedEnvironment);
	}

	private static IndexUpdater createIndexUpdater(EventIndexing eventIndexing, EventFieldIndexing eventFieldIds, boolean multiThreadedEnvironment) {
		return multiThreadedEnvironment
				? new ThreadsafeIndexUpdater(eventIndexing, eventFieldIds)
				: new SimpleIndexUpdater(eventIndexing, eventFieldIds);
	}


	public EventRepoReport report() {
		EventRepoReport report = new EventRepoReport().appendLine(Indexing.class.getSimpleName());
		report.appendReport("EventIndexing", eventIndexing.report());
		report.appendLine("IndexUpdater: " + indexUpdater.getClass().getSimpleName());
		return report;
	}

	/**
	 * Read all indexes that for the given event type that match the given constraint from the given typed event repo
	 * This method will attempt to find the best possible way of retrieving index data given the indexes that exists in this {@link Indexing}
	 * @param eventTypeId {@link Long} event type id
	 * @param constraint {@link FieldConstraint}
	 * @param typedEventRepo {@link TypedEventRepo} where event data can be read from
	 * @return
	 */
	public Iterable<EventId> readIndicies(EventTypeId eventTypeId, FieldConstraint constraint, TypedEventRepo typedEventRepo) {
		if (constraint.hasConstraint()) {
			EventFieldId eventFieldId = new EventFieldId(eventTypeId, constraint.getFieldName());
			if (eventFieldIndexing.isIndexing(eventFieldId)) {
				return eventFieldIndexing.getIterable(eventFieldId, constraint);
			} else {
				return new FallbackFilteringEventIdIterable(eventIndexing.getIndexEntryIterable(eventTypeId), constraint, typedEventRepo);	
			}
		} else {
			return eventIndexing.getIndexEntryIterable(eventTypeId);
		}
	}

	/**
	 * A new event has entered the surrounding {@link EventStore}, let this {@link Indexing} react appropriately
	 * @param eventId <code>long</code>
	 * @param ed The {@link EventSerializer} that is used for creating serialized data for given event type
	 * @param event The event that has entered the {@link EventStore}
	 */
	public void onNewEvent(long eventId, EventSerializer ed, Object event) {
		indexUpdater.onNewEvent(eventId, ed, event);
	}

	/**
	 * Stops this {@link Indexing} and closes all associated files. This indexing is no longer usable after this method has been called.
	 */
	public void stop() {
		eventIndexing.stop();
		eventFieldIndexing.stop();
		indexUpdater.stop();
	}

	@Override
	public String toString() {
		return "Indexing [eventIndexing=" + eventIndexing + ", eventFieldIndexing=" + eventFieldIndexing + ", indexUpdater=" + indexUpdater + "]";
	}

	// ----- Helper classes -----

	public static class EventTypeMatcher implements IndexKeyMatcher {
		private final Set<EventTypeId> acceptedTypes;
		public EventTypeMatcher(Set<EventTypeId> acceptedEventTypes) {
			this.acceptedTypes = acceptedEventTypes;
		}
		public EventTypeMatcher(EventTypeId eventTypeId) {
			this(Collections.singleton(eventTypeId));
		}
		@Override
		public boolean accepts(Object t) {
			return acceptedTypes.contains(new EventTypeId(Long.class.cast(t)));
		}
	}

	private interface IndexUpdater {
		void onNewEvent(long eventId, EventSerializer ed, Object event);
		void stop();
	}

	private static class SimpleIndexUpdater implements IndexUpdater {
		private final EventIndexing eventIndexing;
		private final EventFieldIndexing eventFieldIndexing;

		public SimpleIndexUpdater(EventIndexing eventIndexing, EventFieldIndexing eventFieldIndexing) {
			this.eventIndexing = Objects.requireNonNull(eventIndexing);
			this.eventFieldIndexing = Objects.requireNonNull(eventFieldIndexing);
		}

		@Override
		public void onNewEvent(long eventId, EventSerializer ed, Object event) {
			eventIndexing.onNewEvent(eventId, ed, event);
			eventFieldIndexing.onNewEvent(eventId, ed, event);
		}

		@Override
		public void stop() {
			/* do nothing */
		}

		@Override
		public String toString() {
			return "SimpleIndexUpdater [eventIndexing=" + eventIndexing + ", eventFieldIds=" + eventFieldIndexing + "]";
		}
	}

	private static class ThreadsafeIndexUpdater implements IndexUpdater {
		final EventIndexing eventIndexing;
		final EventFieldIndexing eventFieldIndexing;
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		public ThreadsafeIndexUpdater(EventIndexing eventIndexing, EventFieldIndexing eventFieldIndexing) {
			this.eventIndexing = Objects.requireNonNull(eventIndexing);
			this.eventFieldIndexing = Objects.requireNonNull(eventFieldIndexing);
		}

		@Override
		public void onNewEvent(long eventId, EventSerializer ed, Object event) {
			Future<Void> future = executor.submit(new IndexUpdaterJob(eventId, ed, event));
			try {
				future.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof RuntimeException) {
					throw (RuntimeException) cause;
				} else {
					throw new RuntimeException("Could not execute indexing command", cause);
				}
			}
		}

		@Override
		public void stop() {
			executor.shutdown();
		}

		private class IndexUpdaterJob implements Callable<Void> {
			private final long eventId;
			private final EventSerializer ed;
			private final Object event;

			public IndexUpdaterJob(long eventId, EventSerializer ed, Object event) {
				this.eventId = Objects.requireNonNull(eventId);
				this.ed = Objects.requireNonNull(ed);
				this.event = Objects.requireNonNull(event);
			}

			/**
			 * The critical section
			 */
			@Override
			public Void call() throws Exception {
				eventIndexing.onNewEvent(eventId, ed, event);
				eventFieldIndexing.onNewEvent(eventId, ed, event);
				return null;
			}
		}

	}

}
