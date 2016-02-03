package se.jsa.jles.internal.indexing;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.InternalTypedEventRepo;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.indexing.events.EventIndexing;
import se.jsa.jles.internal.util.Objects;

public class EventIndexPreparationImpl implements EventIndexPreparation {
	private final EventIndexing eventIndexing;
	private final EventDefinitions eventDefinitions;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final EventFile eventFile;

	public EventIndexPreparationImpl(
			EventIndexing eventIndexing, 
			EventDefinitions eventDefinitions,
			EventFile eventFile) {
		this.eventIndexing = Objects.requireNonNull(eventIndexing);
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		this.eventFile = Objects.requireNonNull(eventFile);
	}

	@Override
	public TypedEventRepo getTypedEventRepo(EventTypeId eventTypeId) {
		return new InternalTypedEventRepo(eventTypeId, eventFile, eventDefinitions);
	}

	@Override
	public Iterator<EventId> readIndicies(EventTypeId eventTypeId) {
		return eventIndexing.getIndexEntryIterable(eventTypeId).iterator();
	}

	@Override
	public void schedule(Runnable runnable) {
		executorService.submit(runnable);
	}
}