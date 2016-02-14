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
package se.jsa.jles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import se.jsa.jles.configuration.EntryFileFactoryConfiguration;
import se.jsa.jles.configuration.EventFieldIndexingFactory;
import se.jsa.jles.configuration.EventFieldIndexingFactory.EventFieldIndexConfiguration;
import se.jsa.jles.configuration.EventIndexingConfiguration;
import se.jsa.jles.configuration.EventIndexingMultiFileConfiguration;
import se.jsa.jles.configuration.ThreadingEnvironment;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.eventdefinitions.EventDefinitionFile;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.PersistingEventDefinitions;
import se.jsa.jles.internal.file.EntryFileNameGenerator;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.indexing.EventIndexPreparation;
import se.jsa.jles.internal.indexing.EventIndexPreparationImpl;
import se.jsa.jles.internal.indexing.Indexing;
import se.jsa.jles.internal.indexing.events.EventIndexing;

/**
 * Used for creating fully initialized {@link EventStore} instances
 * @author joakim Joakim Sahlström
 */
public class EventStoreConfigurer {

	public enum WriteStrategy {
		FAST,
		SAFE,
		SUPERSAFE
	}
	
	private static EntryFileNameGenerator entryFileNameGenerator = new EntryFileNameGenerator();

	private final Set<EventFieldIndexConfiguration> indexedEventFields = new HashSet<EventFieldIndexConfiguration>();
	private final AtomicReference<ThreadingEnvironment> threadingEnvironment = new AtomicReference<ThreadingEnvironment>(ThreadingEnvironment.MULTITHREADED);
	private final EntryFileFactoryConfiguration entryFileFactory;
	
	private EventIndexingConfiguration eventIndexConfiguration = EventIndexingMultiFileConfiguration.create();
	private boolean useFileBasedEventDefinitions;

	private EventStoreConfigurer(InMemoryFileRepository inMemoryFileRepository) {
		this.entryFileFactory = new EntryFileFactoryConfiguration(null, inMemoryFileRepository, threadingEnvironment);
		this.useFileBasedEventDefinitions = true;
	}

	private EventStoreConfigurer(FileChannelFactory fileChannelFactory) {
		this.entryFileFactory = new EntryFileFactoryConfiguration(fileChannelFactory, null, threadingEnvironment);
		this.useFileBasedEventDefinitions = true;
	}

	public static EventStoreConfigurer createMemoryOnlyConfigurer() {
		return new EventStoreConfigurer(new InMemoryFileRepository());
	}

	public static EventStoreConfigurer createMemoryOnlyConfigurer(InMemoryFileRepository inMemoryFileRepository) {
		return new EventStoreConfigurer(inMemoryFileRepository);
	}

	public static EventStoreConfigurer createFileBasedConfigurer(FileChannelFactory fileChannelFactory) {
		return new EventStoreConfigurer(fileChannelFactory);
	}

	public EventStoreConfigurer addIndexing(Class<?> eventType, String fieldName) {
		this.indexedEventFields.add(new EventFieldIndexConfiguration(eventType, fieldName, false));
		return this;
	}

	public EventStoreConfigurer addInMemoryIndexing(Class<?> eventType, String fieldName) {
		this.indexedEventFields.add(new EventFieldIndexConfiguration(eventType, fieldName, true));
		return this;
	}

	public EventStoreConfigurer multiThreadedEnvironment() {
		this.threadingEnvironment.set(ThreadingEnvironment.MULTITHREADED);
		return this;
	}

	public EventStoreConfigurer singleThreadedEnvironment() {
		this.threadingEnvironment.set(ThreadingEnvironment.SINGLE_THREAD);
		return this;
	}

	public EventStoreConfigurer testableEventDefinitions() {
		this.useFileBasedEventDefinitions = false;
		return this;
	}

	public EventStoreConfigurer writeStrategy(WriteStrategy writeStrategy) {
		this.entryFileFactory.setWriteStrategy(writeStrategy);
		return this;
	}
	
	public EventStoreConfigurer eventIndexing(EventIndexingConfiguration eventIndexingConfiguration) {
		this.eventIndexConfiguration = eventIndexingConfiguration;
		return this;
	}

	public EventStore configure() {
		EventFile eventFile = new EventFile(entryFileFactory.createEntryFile(entryFileNameGenerator.getEventFileName()));
		EventDefinitions eventDefinitions = createEventDefinitions();
		Indexing indexing = createIndexing(eventDefinitions, eventFile);

		EventStore result = new EventStore(eventFile, indexing, eventDefinitions, threadingEnvironment.get());
		return result;
	}

	private EventDefinitions createEventDefinitions() {
		if (useFileBasedEventDefinitions) {
			PersistingEventDefinitions persistedEventDefinitions = new PersistingEventDefinitions(
					new EventDefinitionFile(entryFileFactory.createEntryFile(entryFileNameGenerator.getEventDefintionsFileName())));
			MappingEventDefinitions eventDefinitions = new MappingEventDefinitions(persistedEventDefinitions);
			eventDefinitions.init();
			return eventDefinitions;
		} else {
			return new MappingEventDefinitions(new MemoryBasedEventDefinitions());
		}
	}

	private Indexing createIndexing(EventDefinitions eventDefinitions, EventFile eventFile) {
		EventIndexing eventIndexing = eventIndexConfiguration.createIndexing(eventDefinitions, entryFileFactory, entryFileNameGenerator);
		EventIndexPreparation preparation = new EventIndexPreparationImpl(eventIndexing, eventDefinitions, eventFile);
		
		EventFieldIndexingFactory eventFieldIndexingFactory = new EventFieldIndexingFactory(preparation, entryFileNameGenerator, entryFileFactory);
		return new Indexing(
				eventIndexing, 
				eventFieldIndexingFactory.createEventFieldIndicies(eventDefinitions, indexedEventFields), 
				threadingEnvironment.get() == ThreadingEnvironment.MULTITHREADED);
	}

	public List<String> getFiles() {
		return entryFileFactory.getFiles();
	}

}