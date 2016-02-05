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

import java.util.Objects;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.file.EntryFileCreator;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.indexing.IndexFile;
import se.jsa.jles.internal.indexing.IndexFile.IndexKeyMatcher;

public class SingleToMultiFileMigrator {

	private EventIndexingMultiFile eventIndexingMultiFile;
	private EventDefinitions eventDefinitions;
	private EntryFileCreator entryFileCreator;
	private EntryFileNameGenerator entryFileNameGenerator;
	
	public SingleToMultiFileMigrator(
			EventIndexingMultiFile eventIndexingMultiFile, 
			EventDefinitions eventDefinitions, 
			EntryFileCreator entryFileCreator,
			EntryFileNameGenerator entryFileNameGenerator) {
		this.eventIndexingMultiFile = Objects.requireNonNull(eventIndexingMultiFile);
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		this.entryFileCreator = Objects.requireNonNull(entryFileCreator);
		this.entryFileNameGenerator = Objects.requireNonNull(entryFileNameGenerator);
	}
	
	public void runAnyMigration() {
		if (isMigrationRequired()) {
			doRunMigration();
		}
	}

	private boolean isMigrationRequired() {
		for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(eventDefinitions.getRegisteredEventTypes())) {
			if (!hasIndexFile(eventTypeId)) {
				return hasEventIndexingSingleFile();
			}
		}
		return false;
	}
	
	private boolean hasIndexFile(EventTypeId eventTypeId) {
		return entryFileCreator.fileExists(entryFileNameGenerator.getMultiFileIndexName(eventTypeId));
	}

	private boolean hasEventIndexingSingleFile() {
		return entryFileCreator.fileExists(entryFileNameGenerator.getEventTypeIndexFileName());
	}

	private void doRunMigration() {
		IndexFile indexFile = new IndexFile(new StorableLongField(), entryFileCreator.createEntryFile(entryFileNameGenerator.getEventTypeIndexFileName()));
		for (EventIndexEntry entry : indexFile.readIndexEntries(IndexKeyMatcher.ALWAYS_MATCHER)) {
			eventIndexingMultiFile.onNewEvent(new EventTypeId((Long) entry.getEventKey()), entry.getEventId().toLong());
		}
	}
	
}
