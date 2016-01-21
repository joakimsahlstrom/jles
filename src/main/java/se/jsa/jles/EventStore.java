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

import java.util.Collections;

import se.jsa.jles.NewEventNotificationListeners.NewEventNotificationListener;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventFile;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.LoadingIterable;
import se.jsa.jles.internal.TypedEventRepo;
import se.jsa.jles.internal.eventdefinitions.MappingEventDefinitions;
import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.indexing.EventFieldIndex;
import se.jsa.jles.internal.indexing.EventIndex;
import se.jsa.jles.internal.indexing.IndexFile;
import se.jsa.jles.internal.indexing.Indexing;
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
	private final NewEventNotificationListeners eventListeners = new NewEventNotificationListeners();

	/**
	 * Create a simplest possible {@link EventStore} using the given {@link EventFile} for event storage and {@link EntryFile} for event type indexing.
	 * Not for production usage.
	 * @param eventFile {@link EventFile}
	 * @param eventTypeIndexFile {@link EntryFile}
	 */
	EventStore(EventFile eventFile, EntryFile eventTypeIndexFile) {
		this(eventFile,
			 new Indexing(
					 new IndexFile(new StorableLongField(), eventTypeIndexFile),
					 Collections.<EventTypeId, EventIndex>emptyMap(),
					 Collections.<EventFieldIndex.EventFieldId, EventFieldIndex>emptyMap(),
					 false),
			 new MappingEventDefinitions(new MemoryBasedEventDefinitions()));
	}

	/**
	 * Used for setting up a fully initialized {@link EventStore}. Use {@link EventStoreConfigurer} instead of this constructor directly
	 * @param eventFile {@link EventFile} file for storing events
	 * @param indexing {@link Indexing} the indexing subsystem
	 * @param eventDefinitions {@link EventDefinitions} The event definitions subsystem
	 */
	EventStore(EventFile eventFile, Indexing indexing, EventDefinitions eventDefinitions) {
		this.eventFile = Objects.requireNonNull(eventFile);
		this.indexing = Objects.requireNonNull(indexing);
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
	}

	public void registerEventListener(NewEventNotificationListener listener) {
		this.eventListeners.registerListener(listener);
	}

	/**
	 * Stores a new event in this {@link EventStore}
	 * @param event The event
	 */
	public void write(Object event) {
		EventSerializer ed = eventDefinitions.getEventSerializer(event);
		long eventId = eventFile.writeEvent(event, ed);
		indexing.onNewEvent(eventId, ed, event);
		eventListeners.onNewEvent();
	}

	public Iterable<Object> readEvents(EventQuery query) {
		LoadingIterable loadingIterable = LoadingIterable.empty();
		EventQuery subQuery = query;
		do {
			for (EventTypeId eventTypeId : eventDefinitions.getEventTypeIds(subQuery.getEventType())) {
				InternalTypedEventRepo typedEventRepo = new InternalTypedEventRepo(eventTypeId);
				Iterable<EventId> iterable = typedEventRepo.getIterator(subQuery.createFieldConstraint());
				loadingIterable = loadingIterable.with(iterable, typedEventRepo);
			}
			subQuery = subQuery.next();
		} while (subQuery != null);
		return loadingIterable;
	}

	/**
	 * Stops this {@link EventStore} instance and releases all associated files.
	 * This instance will no longer be usable after this method has been called.
	 */
	public void stop() {
		indexing.stop();
		eventFile.close();
		eventDefinitions.close();
	}

	// ----- Helper classes -----

	private class InternalTypedEventRepo implements TypedEventRepo {
		private final EventTypeId eventTypeId;
		private final EventDeserializer eventDeserializer;

		public InternalTypedEventRepo(EventTypeId eventTypeId) {
			this.eventTypeId = Objects.requireNonNull(eventTypeId);
			this.eventDeserializer = eventDefinitions.getEventDeserializer(eventTypeId);
		}

		@Override
		public Iterable<EventId> getIterator(FieldConstraint constraint) {
			return indexing.readIndicies(eventTypeId, constraint, this);
		}

		@Override
		public Object readEvent(EventId eventIndex) {
			return eventFile.readEvent(eventIndex.toLong(), eventDeserializer);
		}

		@Override
		public Object readEventField(EventId eventId, String fieldName) {
			EventField eventField = eventDefinitions.getEventField(eventTypeId, fieldName);
			return eventFile.readEventField(eventId.toLong(), eventDeserializer, eventField);
		}

	}

}