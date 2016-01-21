package se.jsa.jles.internal.indexing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.util.Objects;

public class EventIndexPreparationImpl implements EventIndexPreparation {
	private final IndexFile eventTypeIndex;
	private final EventFile eventFile;
	private final EventDefinitions eventDefinitions;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public EventIndexPreparationImpl(IndexFile eventTypeIndex, EventDefinitions eventDefinitions, EventFile eventFile) {
		this.eventTypeIndex = Objects.requireNonNull(eventTypeIndex);
		this.eventFile = Objects.requireNonNull(eventFile);
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
	}

	@Override
	public EventFile getEventFile() {
		return eventFile;
	}

	@Override
	public IndexFile getEventTypeIndex() {
		return eventTypeIndex;
	}

	@Override
	public EventDefinitions getEventDefinitions() {
		return eventDefinitions;
	}

	@Override
	public void schedule(Runnable runnable) {
		executorService.submit(runnable);
	}

	@Override
	public void prepare(Map<EventTypeId, EventIndex> eventIndicies) {
		Map<EventTypeId, Iterator<EventId>> existingIndicies = toIteratorMap(eventIndicies);
		Iterator<EventIndexEntry> sourceIndicies = eventTypeIndex.readIndexEntries(new Indexing.EventTypeMatcher(eventIndicies.keySet())).iterator();

		while (sourceIndicies.hasNext()) {
			EventIndexEntry eventIndexEntry = sourceIndicies.next();
			EventTypeId eventTypeId = new EventTypeId((Long) eventIndexEntry.getEventKey());

			EventId expectedEventId = eventIndexEntry.getEventId();
			if (!existingIndicies.get(eventTypeId).hasNext()) {
				eventIndicies.get(eventTypeId).writeIndex(expectedEventId.toLong());
				existingIndicies.get(eventTypeId).next(); // read what was just written!
			} else {
				verifyEventId(existingIndicies.get(eventTypeId), eventTypeId, expectedEventId);
			}
		}

		verifyNoExtraEventIndexes(existingIndicies);
	}

	private Map<EventTypeId, Iterator<EventId>> toIteratorMap(Map<EventTypeId, EventIndex> eventIndicies) {
		Map<EventTypeId, Iterator<EventId>> result = new HashMap<EventTypeId, Iterator<EventId>>();
		for (Map.Entry<EventTypeId, EventIndex> e : eventIndicies.entrySet()) {
			result.put(e.getKey(), e.getValue().readIndicies().iterator());
		}
		return result;
	}

	private void verifyEventId(Iterator<EventId> existingIndicies, EventTypeId eventTypeId, EventId expectedEventId) {
		EventId actualEventId = existingIndicies.next();
		if (!expectedEventId.equals(actualEventId)) {
			throw new RuntimeException("Indexing between event index and source event type index did not match for eventTypeId=" + eventTypeId
					+ ". Expected=" + expectedEventId
					+ " Actual=" + actualEventId);
		}
	}

	private void verifyNoExtraEventIndexes(Map<EventTypeId, Iterator<EventId>> existingIndicies) {
		// any EventIndex that still has events in them are incorrect compared to source event index file!
		for (Map.Entry<EventTypeId, Iterator<EventId>> e : existingIndicies.entrySet()) {
			if (e.getValue().hasNext()) {
				throw new RuntimeException("Index for eventType " + e.getKey() + " contains more indexes than the source event type index");
			}
		}
	}
}