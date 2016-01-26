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
package se.jsa.jles.internal.indexing.events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.EntryFileCreator;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.indexing.IndexFile;
import se.jsa.jles.internal.indexing.Indexing;
import se.jsa.jles.internal.indexing.Indexing.EventTypeMatcher;
import se.jsa.jles.internal.util.Objects;

public class EventIndexingSingleFile implements EventIndexing {

	private final IndexFile eventTypeIndexFile;
	private final Map<EventTypeId, EventIndex> eventIndicies;

	public EventIndexingSingleFile(IndexFile eventTypeIndexFile, Map<EventTypeId, EventIndex> eventIndicies) {
		this.eventTypeIndexFile = Objects.requireNonNull(eventTypeIndexFile);
		this.eventIndicies = Objects.requireNonNull(eventIndicies);
	}

	public static EventIndexingSingleFile create(
			Set<Class<?>> indexedEventTypes,
			EntryFileCreator entryFileCreator,
			EntryFileNameGenerator entryFileNameGenerator,
			EventDefinitions eventDefinitions) {
		IndexFile eventTypeIndex = new IndexFile(new StorableLongField(), entryFileCreator.createEntryFile(entryFileNameGenerator.getEventTypeIndexFileName()));
		Map<EventTypeId, EventIndex> eventIndicies = createEventIndicies(indexedEventTypes, entryFileCreator, entryFileNameGenerator, eventDefinitions);

		EventIndexingSingleFile eventIndexing = new EventIndexingSingleFile(eventTypeIndex, eventIndicies);
		eventIndexing.prepare();

		return eventIndexing;
	}

	private static Map<EventTypeId, EventIndex> createEventIndicies(
			Set<Class<?>> indexedEventTypes,
			EntryFileCreator entryFileCreator,
			EntryFileNameGenerator entryFileNameGenerator,
			EventDefinitions eventDefinitions) {
		Map<EventTypeId, EventIndex> eventIndicies = new HashMap<EventTypeId, EventIndex>();
		for (Class<?> indexedEventType : indexedEventTypes) {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(indexedEventType)) {
				EventIndex eventIndex = new EventIndex(entryFileCreator.createEntryFile(entryFileNameGenerator.getEventIndexFileName(eventTypeId)), eventTypeId);
				eventIndicies.put(eventTypeId, eventIndex);
			}
		}
		return eventIndicies;
	}

	@Override
	public void prepare() {
		Map<EventTypeId, Iterator<EventId>> existingIndicies = toIteratorMap(eventIndicies);
		Iterator<EventIndexEntry> sourceIndicies = eventTypeIndexFile.readIndexEntries(new Indexing.EventTypeMatcher(eventIndicies.keySet())).iterator();

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


	@Override
	public void onNewEvent(long eventId, EventSerializer ed, Object event) {
		eventTypeIndexFile.writeIndex(eventId, ed.getEventTypeId().toLong());
		if (eventIndicies.containsKey(ed.getEventTypeId())) {
			eventIndicies.get(ed.getEventTypeId()).writeIndex(eventId);
		}
	}

	@Override
	public Iterable<EventId> getIndexEntryIterable(EventTypeId eventTypeId) {
		if (eventIndicies.containsKey(eventTypeId)) {
			return eventIndicies.get(eventTypeId).readIndicies();
		}
		return eventTypeIndexFile.readIndicies(new EventTypeMatcher(eventTypeId));
	}

	@Override
	public void stop() {
		eventTypeIndexFile.close();
		for (EventIndex ei : eventIndicies.values()) {
			ei.close();
		}
	}

	@Override
	public EventRepoReport report() {
		EventRepoReport report = new EventRepoReport().appendLine(EventIndexingSingleFile.class.getSimpleName());
		report.appendReport("IndexFile", eventTypeIndexFile.report());
		for (EventIndex eventIndex : eventIndicies.values()) {
			report.appendReport("EventIndex " + eventIndex.getEventTypeId().toString(), eventIndex.report());
		}
		return report;
	}

}
