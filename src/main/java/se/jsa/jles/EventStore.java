package se.jsa.jles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFieldConstraint;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventIndex;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.IndexType;
import se.jsa.jles.internal.Indexing;
import se.jsa.jles.internal.LoadingIterable;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.StorableLongField;
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
	private final EventWriter eventWriter;

	EventStore(EventFile eventFile, EntryFile eventTypeIndexFile) {
		this(eventFile,
			 new Indexing(new IndexFile(new StorableLongField(), eventTypeIndexFile), Collections.<Long, EventIndex>emptyMap(), Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap(), false),
			 new MappingEventDefinitions(new MemoryBasedEventDefinitions()));
	}

	EventStore(EventFile eventFile, Indexing indexing, EventDefinitions eventDefinitions) {
		this.eventFile = eventFile;
		this.indexing = indexing;
		this.eventDefinitions = eventDefinitions;
		this.eventWriter = createEventWriter(eventFile, indexing);
	}

	private static EventWriter createEventWriter(EventFile eventFile, Indexing indexing) {
		return new SimpleEventWriter(eventFile, indexing);
	}

	public void write(Object event) {
		EventSerializer ed = eventDefinitions.getEventSerializer(event);
		eventWriter.onNewEvent(event, ed);
	}

	public List<Object> collectEvents(Class<?>... eventTypes) {
		return collect(readEvents(eventTypes));
	}

	public static List<Object> collect(Iterable<Object> iterable) {
		List<Object> result = new ArrayList<Object>();
		for (Object event : iterable) {
			result.add(event);
		}
		return result;
	}

	public Iterable<Object> readEvents(Class<?>... eventTypes) {
		LoadingIterable loadingIterable = new LoadingIterable();
		for (Long eventTypeId : eventDefinitions.getEventTypeIds(eventTypes)) {
			InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
			Iterable<EventId> iterable = typedEventRepo.getIterator(EventFieldConstraint.noConstraint());
			loadingIterable.register(iterable, typedEventRepo);
		}
		return loadingIterable;
	}

	public Iterable<Object> readEvents(Class<?> eventType, Match match) {
		LoadingIterable loadingIterable = new LoadingIterable();
		for (Long eventTypeId : eventDefinitions.getEventTypeIds(eventType)) {
			InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
			Iterable<EventId> iterable = match.buildFilteringIterator(typedEventRepo);
			loadingIterable.register(iterable, typedEventRepo);
		}
		return loadingIterable;
	}

	public void stop() {
		eventWriter.stop();
		indexing.stop();
		eventFile.close();
		eventDefinitions.close();
	}

	// ----- Helper methods -----

	private class InternalTypedEventRepo implements TypedEventRepo {
		private final Long eventTypeId;
		private final EventDeserializer eventDeserializer;

		public InternalTypedEventRepo(Long eventTypeId) {
			this.eventTypeId = Objects.requireNonNull(eventTypeId);
			this.eventDeserializer = eventDefinitions.getEventDeserializer(eventTypeId);
		}

		@Override
		public Iterable<EventId> getIterator(EventFieldConstraint constraint) {
			return indexing.readIndicies(eventTypeId, constraint, this);
		}

		@Override
		public Object readEvent(EventId eventIndex) {
			return eventFile.readEvent(eventIndex.getEventId(), eventDeserializer);
		}

		@Override
		public Object readEventField(EventId eventIndex, String fieldName) {
			return eventDefinitions.getEventField(eventTypeId, fieldName).getValue(readEvent(eventIndex)); // non indexed
		}

		@Override
		public IndexType getIndexing(String fieldName) {
			return fieldName.equals("First") ? IndexType.SIMPLE : IndexType.NONE; // TODO: Fix this
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

	/* Remove this once sure that it is not necessary to maintain correct state in a multithreaded environment */
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
