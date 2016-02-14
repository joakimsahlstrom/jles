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
package se.jsa.jles.configuration;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.file.EntryFileCreator;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.indexing.events.EventIndexing;
import se.jsa.jles.internal.indexing.events.EventIndexingMultiFile;

public final class EventIndexingConfigurationMultiFile implements EventIndexingConfiguration {

	private EventIndexingConfigurationMultiFile() {
		// hide this
	}
	
	public static EventIndexingConfiguration create() {
		return new EventIndexingConfigurationMultiFile();
	}
	
	@Override
	public EventIndexing createIndexing(EventDefinitions eventDefinitions, EntryFileCreator entryFileFactory, EntryFileNameGenerator entryFileNameGenerator) {
		return EventIndexingMultiFile.create(entryFileFactory, entryFileNameGenerator, eventDefinitions);
	}

}
