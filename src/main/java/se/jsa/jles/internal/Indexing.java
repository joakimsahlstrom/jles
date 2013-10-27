package se.jsa.jles.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import se.jsa.jles.internal.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.IndexFile.IndexKeyMatcher;
import se.jsa.jles.internal.util.Objects;

/**
 * The class responsible for maintaining the overall indexing of jles
 * @author joakim Joakim Sahlström
 *
 */
public class Indexing {
	private final IndexFile eventTypeIndexFile;
	private final Map<Long, EventIndex> eventIndicies;
	private final Map<EventFieldId, EventFieldIndex> eventFieldIndicies;
	private final IndexUpdater indexUpdater;

	public Indexing(IndexFile eventTypeIndexFile, Map<Long, EventIndex> eventIndicies, Map<EventFieldId, EventFieldIndex> eventFieldIds, boolean multiThreadedEnvironment) {
		this.eventTypeIndexFile = eventTypeIndexFile;
		this.eventIndicies = Objects.requireNonNull(eventIndicies);
		this.eventFieldIndicies = Objects.requireNonNull(eventFieldIds);
		this.indexUpdater = createIndexUpdater(eventTypeIndexFile, eventIndicies, eventFieldIds, multiThreadedEnvironment);
	}

	private static IndexUpdater createIndexUpdater(IndexFile eventTypeIndexFile, Map<Long, EventIndex> eventIndicies, Map<EventFieldId, EventFieldIndex> eventFieldIds, boolean multiThreadedEnvironment) {
		return multiThreadedEnvironment ? new ThreadsafeIndexUpdater(eventTypeIndexFile, eventIndicies, eventFieldIds) : new SimpleIndexUpdater(eventTypeIndexFile, eventIndicies, eventFieldIds);
	}

	public Iterable<EventId> readIndicies(Long eventTypeId, EventFieldConstraint constraint, TypedEventRepo typedEventRepo) {
		if (!constraint.hasConstraint()) {
			return getIndexEntryIterable(eventTypeId);
		}
		EventFieldId eventFieldId = new EventFieldId(eventTypeId, constraint.getFieldName());
		if (eventFieldIndicies.containsKey(eventFieldId)) {
			return eventFieldIndicies.get(eventFieldId).getIterable(constraint);
		} else {
			Iterable<EventId> baseIter = getIndexEntryIterable(eventTypeId);
			return new FallbackFilteringEventIdIterable(baseIter, constraint, typedEventRepo);
		}
	}

	private Iterable<EventId> getIndexEntryIterable(Long eventTypeId) {
		if (eventIndicies.containsKey(eventTypeId)) {
			return eventIndicies.get(eventTypeId).readIndicies();
		}
		return eventTypeIndexFile.readIndicies(new EventTypeMatcher(eventTypeId));
	}

	public void onNewEvent(long eventId, EventSerializer ed, Object event) {
		indexUpdater.onNewEvent(eventId, ed, event);
	}

	public IndexType getIndexing(Long eventTypeId, String fieldName) {
		if (eventFieldIndicies.containsKey(new EventFieldId(eventTypeId, fieldName))) {
			return IndexType.SIMPLE; // Advanced indexing not yet supported
		}
		return IndexType.NONE;
	}

	public void stop() {
		eventTypeIndexFile.close();
		for (EventIndex ei : eventIndicies.values()) {
			ei.close();
		}
		for (EventFieldIndex efi : eventFieldIndicies.values()) {
			efi.close();
		}
	}

	@Override
	public String toString() {
		return "Indexing [eventTypeIndexFile=" + eventTypeIndexFile + ", eventIndicies=" + eventIndicies + ", eventFieldIds=" + eventFieldIndicies + "]";
	}

	// ----- Helper classes -----

	public static class EventTypeMatcher implements IndexKeyMatcher {
		private final Set<Long> acceptedTypes;
		public EventTypeMatcher(Set<Long> acceptedEventTypes) {
			this.acceptedTypes = acceptedEventTypes;
		}
		public EventTypeMatcher(Long eventTypeId) {
			this(Collections.singleton(eventTypeId));
		}
		@Override
		public boolean accepts(Object t) {
			return acceptedTypes.contains(Long.class.cast(t));
		}
	}

	private interface IndexUpdater {
		void onNewEvent(long eventId, EventSerializer ed, Object event);
		void stop();
	}

	private static class SimpleIndexUpdater implements IndexUpdater {
		private final IndexFile eventTypeIndexFile;
		private final Map<Long, EventIndex> eventIndicies;
		private final Map<EventFieldId, EventFieldIndex> eventFieldIds;

		public SimpleIndexUpdater(IndexFile eventTypeIndexFile, Map<Long, EventIndex> eventIndicies, Map<EventFieldId, EventFieldIndex> eventFieldIds) {
			this.eventTypeIndexFile = Objects.requireNonNull(eventTypeIndexFile);
			this.eventIndicies = Objects.requireNonNull(eventIndicies);
			this.eventFieldIds = Objects.requireNonNull(eventFieldIds);
		}

		@Override
		public void onNewEvent(long eventId, EventSerializer ed, Object event) {
			eventTypeIndexFile.writeIndex(eventId, ed.getEventTypeId());
			if (eventIndicies.containsKey(ed.getEventTypeId())) {
				eventIndicies.get(ed.getEventTypeId()).writeIndex(eventId);
			}
			for (EventFieldIndex efi : eventFieldIds.values()) {
				if (efi.indexes(ed.getEventTypeId())) {
					efi.onNewEvent(eventId, event);
				}
			}
		}

		@Override
		public void stop() {
			/* do nothing */
		}

		@Override
		public String toString() {
			return "SimpleIndexUpdater [eventTypeIndexFile=" + eventTypeIndexFile + ", eventIndicies=" + eventIndicies + ", eventFieldIds=" + eventFieldIds + "]";
		}
	}

	private static class ThreadsafeIndexUpdater implements IndexUpdater {
		final IndexFile eventTypeIndexFile;
		final Map<Long, EventIndex> eventIndicies;
		final Map<EventFieldId, EventFieldIndex> eventFieldIds;
		private final ExecutorService executor = Executors.newSingleThreadExecutor();

		public ThreadsafeIndexUpdater(IndexFile eventTypeIndexFile, Map<Long, EventIndex> eventIndicies, Map<EventFieldId, EventFieldIndex> eventFieldIds) {
			this.eventTypeIndexFile = Objects.requireNonNull(eventTypeIndexFile);
			this.eventIndicies = Objects.requireNonNull(eventIndicies);
			this.eventFieldIds = Objects.requireNonNull(eventFieldIds);
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

			@Override
			public Void call() throws Exception {
				eventTypeIndexFile.writeIndex(eventId, ed.getEventTypeId());
				if (eventIndicies.containsKey(ed.getEventTypeId())) {
					eventIndicies.get(ed.getEventTypeId()).writeIndex(eventId);
				}
				for (EventFieldIndex efi : eventFieldIds.values()) {
					if (efi.indexes(ed.getEventTypeId())) {
						efi.onNewEvent(eventId, event);
					}
				}
				return null;
			}
		}

	}

}
