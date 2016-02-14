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

import java.util.HashSet;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.file.EntryFileCreator;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.indexing.events.EventIndexing;
import se.jsa.jles.internal.indexing.events.EventIndexingSingleFile;

public final class EventIndexingSingleFileConfiguration implements EventIndexingConfiguration {

	private final Set<Class<?>> indexedEventTypes = new HashSet<Class<?>>();

	private EventIndexingSingleFileConfiguration() {
		// hide this
	}
	
	public static EventIndexingSingleFileConfiguration create() {
		return new EventIndexingSingleFileConfiguration();
	}
	
	public EventIndexingSingleFileConfiguration addIndexing(Class<?> eventType) {
		indexedEventTypes.add(eventType);
		return this;
	}
	
	@Override
	public EventIndexing createIndexing(EventDefinitions eventDefinitions, EntryFileCreator entryFileFactory, EntryFileNameGenerator entryFileNameGenerator) {
		return EventIndexingSingleFile.create(indexedEventTypes, entryFileFactory, entryFileNameGenerator, eventDefinitions);
	}
	
}
