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
import java.util.Map;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.file.EntryFileCreator;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.util.Objects;

public class EventIndexingMultiFile implements EventIndexing {

	private final Map<EventTypeId, EventIndex> indicies = new HashMap<EventTypeId, EventIndex>();
	private EntryFileCreator entryFileCreator;
	private EntryFileNameGenerator entryFileNameGenerator;
	
	public EventIndexingMultiFile(EntryFileCreator entryFileCreator, EntryFileNameGenerator entryFileNameGenerator) {
		this.entryFileCreator = Objects.requireNonNull(entryFileCreator);
		this.entryFileNameGenerator = Objects.requireNonNull(entryFileNameGenerator);
	}
	
	public static EventIndexing create(
			EntryFileCreator entryFileCreator,
			EntryFileNameGenerator entryFileNameGenerator, 
			EventDefinitions eventDefinitions) {
		EventIndexingMultiFile eventIndexingMultiFile = new EventIndexingMultiFile(entryFileCreator, entryFileNameGenerator);
		eventIndexingMultiFile.prepare(eventDefinitions);
		return eventIndexingMultiFile;
	}

	@Override
	public void prepare(EventDefinitions eventDefinitions) {
		new SingleToMultiFileMigrator(this, eventDefinitions, entryFileCreator, entryFileNameGenerator)
			.runAnyMigration();
	}
	
	@Override
	public void onNewEvent(long eventId, EventSerializer ed, Object event) {
		onNewEvent(ed.getEventTypeId(), eventId);
	}

	void onNewEvent(EventTypeId eventTypeId, long eventId) {
		getEventIndex(eventTypeId).writeIndex(eventId);
	}

	@Override
	public Iterable<EventId> getIndexEntryIterable(EventTypeId eventTypeId) {
		return getEventIndex(eventTypeId).readIndicies();
	}

	@Override
	public void stop() {
		for (EventIndex eventIndex : indicies.values()) {
			eventIndex.close();
		}
	}

	@Override
	public EventRepoReport report() {
		EventRepoReport report = new EventRepoReport().appendLine(EventIndexingMultiFile.class.getSimpleName());
		for (Map.Entry<EventTypeId, EventIndex> e : indicies.entrySet()) {
			report.appendReport("EventType:" + e.getKey().toLong(), e.getValue().report());
		}
		return report;
	}
	
	private EventIndex getEventIndex(EventTypeId eventTypeId) {
		if (!indicies.containsKey(eventTypeId)) {
			createEventIndex(eventTypeId);
		}
		return indicies.get(eventTypeId);
	}

	private synchronized void createEventIndex(EventTypeId eventTypeId) {
		if (indicies.containsKey(eventTypeId)) {
			return;
		}
		EntryFile entryFile = entryFileCreator.createEntryFile(entryFileNameGenerator.getMultiFileIndexName(eventTypeId));
		indicies.put(eventTypeId, new EventIndex(entryFile, eventTypeId));
	}

}
