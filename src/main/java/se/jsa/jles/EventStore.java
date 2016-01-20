package se.jsa.jles;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import se.jsa.jles.NewEventNotificationListeners.NewEventNotificationListener;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.IndexType;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.LoadingIterable;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.util.Objects;
import se.jsa.jles.internal.util.ReflectionUtil;


/**
 * The class defining and implementing methods for basic interactions with jles
 * @author joakim Joakim Sahlström
 *
 */
public class EventStore {
	private final EventFile eventFile; // Wrap this in "Events"?
	private final Indexing indexing;
	private final EventDefinitions eventDefinitions;
	private final EventWriter eventWriter;
	private final NewEventNotificationListeners eventListeners = new NewEventNotificationListeners();

	/**
	 * Create a simplest possible {@link EventStore} using the given {@link EventFile} for event storage and {@link EntryFile} for event type indexing.
	 * Not for production usage.
	 * @param eventFile {@link EventFile}
	 * @param eventTypeIndexFile {@link EntryFile}
	 */
	EventStore(EventFile eventFile, EntryFile eventTypeIndexFile) {
		this(eventFile,
			 new Indexing(new IndexFile(new StorableLongField(), eventTypeIndexFile), Collections.<EventTypeId, EventIndex>emptyMap(), Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap(), false),
			 new MappingEventDefinitions(new MemoryBasedEventDefinitions()));
	}

	/**
	 * Used for setting up a fully initialized {@link EventStore}. Use {@link EventStoreConfigurer} instead of this constructor directly
	 * @param eventFile {@link EventFile} file for storing events
	 * @param indexing {@link Indexing} the indexing subsystem
	 * @param eventDefinitions {@link EventDefinitions} The event definitions subsystem
	 */
	EventStore(EventFile eventFile, Indexing indexing, EventDefinitions eventDefinitions) {
		this.eventFile = Objects.requireNonNull(eventFile);
		this.indexing = Objects.requireNonNull(indexing);
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		this.eventWriter = createEventWriter(eventFile, indexing);
	}

	private static EventWriter createEventWriter(EventFile eventFile, Indexing indexing) {
		return new SimpleEventWriter(eventFile, indexing);
	}

	public void registerEventListener(NewEventNotificationListener listener) {
		this.eventListeners.registerListener(listener);
	}

	/**
	 * Stores a new event in this {@link EventStore}
	 * @param event The event
	 */
	public void write(Object event) {
		EventSerializer ed = eventDefinitions.getEventSerializer(event);
		eventWriter.onNewEvent(event, ed);
		eventListeners.onNewEvent();
	}

	public Iterable<Object> readEvents(EventQuery query) {
		LoadingIterable loadingIterable = LoadingIterable.empty();
		do {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(query.getEventType())) {
				InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
				Iterable<EventId> iterable = typedEventRepo.getIterator(query.createFieldConstraint());
				loadingIterable = loadingIterable.with(iterable, typedEventRepo);
			}
			query = query.next();
		} while (query != null);
		return loadingIterable;
	}

	/**
	 * Stops this {@link EventStore} instance and releases all associated files.
	 * This instance will no longer be usable after this method has been called.
	 */
	public void stop() {
		eventWriter.stop();
		indexing.stop();
		eventFile.close();
		eventDefinitions.close();
	}

	// ----- Helper classes -----

	private class InternalTypedEventRepo implements TypedEventRepo {
		private final EventTypeId eventTypeId;
		private final EventDeserializer eventDeserializer;

		public InternalTypedEventRepo(EventTypeId eventTypeId) {
			this.eventTypeId = Objects.requireNonNull(eventTypeId);
			this.eventDeserializer = eventDefinitions.getEventDeserializer(eventTypeId);
		}

		@Override
		public Iterable<EventId> getIterator(FieldConstraint constraint) {
			return indexing.readIndicies(eventTypeId, constraint, this);
		}

		@Override
		public Object readEvent(EventId eventIndex) {
			return eventFile.readEvent(eventIndex.toLong(), eventDeserializer);
		}

		@Override
		public Object readEventField(EventId eventIndex, String fieldName) {
//			EventField eventField = eventDefinitions.getEventField(eventTypeId, fieldName);
			Object event = eventFile.readEvent(eventIndex.toLong(), eventDeserializer);
			Method getMethod = ReflectionUtil.getPropertyRetrieveMethod(event.getClass(), fieldName);
			try {
				return getMethod.invoke(event);
			} catch (Exception e) {
				throw new RuntimeException("Unknown event field!");
			}
			//return eventFile.readEventField(eventIndex.getEventId(), eventDeserializer, eventField);
		}

		@Override
		public IndexType getIndexing(String fieldName) {
			return indexing.getIndexing(eventTypeId, fieldName);
		}
	}

	// Is this really necessary or should the thread safing be done in Indexing instead?
	private interface EventWriter {
		void onNewEvent(Object event, EventSerializer ed);
		void stop();
	}

	private static class SimpleEventWriter implements EventWriter {
		private final EventFile eventFile;
		private final Indexing indexing;

		public SimpleEventWriter(EventFile eventFile, Indexing indexing) {
			this.eventFile = Objects.requireNonNull(eventFile);
			this.indexing = Objects.requireNonNull(indexing);
		}

		@Override
		public void onNewEvent(Object event, EventSerializer ed) {
			long eventId = eventFile.writeEvent(event, ed);
			indexing.onNewEvent(eventId, ed, event);
		}

		@Override
		public void stop() {
			/* do nothing */
		}

		@Override
		public String toString() {
			return "SimpleEventWriter [eventFile=" + eventFile + ", indexing=" + indexing + "]";
		}
	}

	@SuppressWarnings("unused")
	private static class ThreadsafeEventWriter implements EventWriter {
		final EventFile eventFile;
		final Indexing indexing;
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		public ThreadsafeEventWriter(EventFile eventFile, Indexing indexing) {
			this.eventFile = Objects.requireNonNull(eventFile);
			this.indexing = Objects.requireNonNull(indexing);
		}

		@Override
		public void onNewEvent(Object event, EventSerializer ed) {
			Future<Void> future = executor.submit(new WriteEventJob(event, ed));
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
		}

		private class WriteEventJob implements Callable<Void> {
			private final Object event;
			private final EventSerializer ed;

			public WriteEventJob(Object event, EventSerializer ed) {
				this.event = Objects.requireNonNull(event);
				this.ed = Objects.requireNonNull(ed);
			}

			@Override
			public Void call() throws Exception {
				long eventId = eventFile.writeEvent(event, ed);
				indexing.onNewEvent(eventId, ed, event);
				return null;
			}
		}

		@Override
		public String toString() {
			return "ThreadsafeEventWriter [eventFile=" + eventFile + ", indexing=" + indexing + ", executor=" + executor + "]";
		}
	}

}