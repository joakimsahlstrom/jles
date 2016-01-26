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
package se.jsa.jles;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import se.jsa.jles.NewEventNotificationListeners.NewEventNotificationListener;
import se.jsa.jles.configuration.ThreadingEnvironment;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.InternalTypedEventRepo;
import se.jsa.jles.internal.LoadingIterable;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.indexing.IndexFile;
import se.jsa.jles.internal.indexing.Indexing;
import se.jsa.jles.internal.indexing.events.EventIndex;
import se.jsa.jles.internal.indexing.events.EventIndexingSingleFile;
import se.jsa.jles.internal.indexing.fields.EventFieldIndex;
import se.jsa.jles.internal.util.Objects;


/**
 * The class defining and implementing methods for basic interactions with jles
 * @author joakim Joakim Sahlström
 *
 */
public class EventStore {
	private final EventFile eventFile; // Wrap this in "Events"?
	private final Indexing indexing;
	private final EventDefinitions eventDefinitions;
	private final NewEventNotificationListeners eventListeners = new NewEventNotificationListeners();
	private final EventRegistrator eventRegistrator;

	/**
	 * Create a simplest possible {@link EventStore} using the given {@link EventFile} for event storage and {@link EntryFile} for event type indexing.
	 * Not for production usage.
	 * @param eventFile {@link EventFile}
	 * @param eventTypeIndexFile {@link EntryFile}
	 */
	EventStore(EventFile eventFile, EntryFile eventTypeIndexFile, ThreadingEnvironment threadingEnvironment) {
		this(eventFile,
			 new Indexing(
					 new EventIndexingSingleFile(
							 new IndexFile(new StorableLongField(), eventTypeIndexFile),
							 Collections.<EventTypeId, EventIndex>emptyMap()),
					 Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap(),
					 threadingEnvironment == ThreadingEnvironment.MULTITHREADED),
			 new MappingEventDefinitions(new MemoryBasedEventDefinitions()),
			 threadingEnvironment);
	}

	/**
	 * Used for setting up a fully initialized {@link EventStore}. Use {@link EventStoreConfigurer} instead of this constructor directly
	 * @param eventFile {@link EventFile} file for storing events
	 * @param indexing {@link Indexing} the indexing subsystem
	 * @param eventDefinitions {@link EventDefinitions} The event definitions subsystem
	 */
	EventStore(EventFile eventFile, Indexing indexing, EventDefinitions eventDefinitions, ThreadingEnvironment threadingEnvironment) {
		this.eventFile = Objects.requireNonNull(eventFile);
		this.indexing = Objects.requireNonNull(indexing);
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		this.eventRegistrator = threadingEnvironment == ThreadingEnvironment.SINGLE_THREAD ? new UnsynchronizedEventRegistrator() : new SynchronizedEventRegistrator();
	}

	public void registerEventListener(NewEventNotificationListener listener) {
		this.eventListeners.registerListener(listener);
	}

	/**
	 * Stores a new event in this {@link EventStore}
	 * @param event The event
	 */
	public void write(Object event) {
		eventRegistrator.register(event);
		eventListeners.onNewEvent();
	}

	public Iterable<Object> readEvents(EventQuery query) {
		LoadingIterable loadingIterable = LoadingIterable.empty();
		EventQuery subQuery = query;
		do {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(subQuery.getEventType())) {
				InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId, eventFile, eventDefinitions);
				Iterable<EventId> iterable = indexing.readIndicies(eventTypeId, subQuery.createFieldConstraint(), typedEventRepo);
				loadingIterable = loadingIterable.with(iterable, typedEventRepo);
			}
			subQuery = subQuery.next();
		} while (subQuery != null);
		return loadingIterable;
	}

	/**
	 * Stops this {@link EventStore} instance and releases all associated files.
	 * This instance will no longer be usable after this method has been called.
	 */
	public void stop() {
		indexing.stop();
		eventRegistrator.stop();
		eventFile.close();
		eventDefinitions.close();
	}

	public EventRepoReport report() {
		return new EventRepoReport()
			.appendLine("EventStore report")
			.appendLine("==================")
			.appendReport("EventFile", eventFile.report())
			.appendReport("EventDefinitions", eventDefinitions.report())
			.appendReport("Indexing", indexing.report());
	}

	// ----- Helper classes -----

	private class EventRegistration implements Callable<Void> {
		private final Object event;

		public EventRegistration(Object event) {
			this.event = event;
		}

		@Override
		public Void call() {
			EventSerializer ed = eventDefinitions.getEventSerializer(event);
			long eventId = eventFile.writeEvent(event, ed);
			indexing.onNewEvent(eventId, ed, event);
			return null;
		}
	}

	private interface EventRegistrator {
		void register(Object event);
		void stop();
	}

	private class UnsynchronizedEventRegistrator implements EventRegistrator {
		public UnsynchronizedEventRegistrator() {
		}

		@Override
		public void register(Object event) {
			new EventRegistration(event).call();
		}

		@Override
		public void stop() {
			// do nothing
		}
	}

	private class SynchronizedEventRegistrator implements EventRegistrator  {
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		public SynchronizedEventRegistrator() {
		}

		@Override
		public void register(Object event) {
			Future<Void> future = executor.submit(new EventRegistration(event));
			try {
				future.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause instanceof RuntimeException) {
					throw (RuntimeException) cause;
				} else {
					throw new RuntimeException("Could not execute append command", cause);
				}
			}
		}

		@Override
		public void stop() {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException("Awaiting event registration shutdown failed!", e);
			}
		}
	}

}