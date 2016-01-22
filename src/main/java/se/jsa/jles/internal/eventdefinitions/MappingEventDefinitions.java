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
package se.jsa.jles.internal.eventdefinitions;

import java.util.Set;

import se.jsa.jles.EventRepoReport;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

/**
 * Used for employing a different type for persisting events compared to the event types present in the system otherwise.
 * Very usable to allow for event type versioning and to avoid the restrictions that jles otherwise imposes on event classes.
 * @author joakim
 *
 */
public class MappingEventDefinitions implements EventDefinitions {
	private final EventDefinitions definitions;
	private final EventResolver eventResolver;

	/**
	 * This class acts as a wrapper class for the {@link EventDefinitions} that is doing all the actual event definition work
	 * Uses the {@link ConventionbasedEventResolver} for event type resolution
	 * @param definitions wrapped {@link EventDefinitions}
	 */
	public MappingEventDefinitions(EventDefinitions definitions) {
		this(definitions, new ConventionbasedEventResolver());
	}

	/**
	 * Create a new {@link MappingEventDefinitions} with a customer {@link EventResolver}
	 * @param definitions wrapped {@link EventDefinitions}
	 * @param eventResolver The customer {@link EventResolver}
	 */
	MappingEventDefinitions(EventDefinitions definitions, EventResolver eventResolver) {
		this.definitions = Objects.requireNonNull(definitions);
		this.eventResolver = Objects.requireNonNull(eventResolver);
	}

	/**
	 * Must be run after instantiating and before any usage to have this class function properly
	 */
	@Override
	public void init() {
		definitions.init();
		eventResolver.registerEventTypes(definitions.getRegisteredEventTypes());
	}

	/**
	 * Closes this {@link EventDefinitions} and all associated resources. This instance can no longer be used after this method has been called.
	 */
	@Override
	public void close() {
		definitions.close();
	}

	/**
	 * Get an array of all {@link Class}es stored in this {@link EventDefinitions}
	 */
	@Override
	public Class<?>[] getRegisteredEventTypes() {
		return definitions.getRegisteredEventTypes(); // TODO: Is this correct? should we not use the unmapped event defintions from ent EventResolver instead?
	}

	/**
	 * Get all event types ids (as a {@link Set} of {@link Long} for the given event types
	 * @param varargs {@link Class} of event types
	 * @return {@link Set} of {@link Long} of matched event type ids
	 */
	@Override
	public Set<EventTypeId> getEventTypeIds(Class<?>... eventTypes) {
		return definitions.getEventTypeIds(eventResolver.getSerializableEventTypes(eventTypes));
	}

	/**
	 * Get the {@link EventSerializer} for the given event
	 * @param Event
	 * @return {@link EventSerializer}
	 */
	@Override
	public EventSerializer getEventSerializer(Object event) {
		return eventResolver.wrapSerializer(definitions.getEventSerializer(eventResolver.getSerializableEvent(event)));
	}

	/**
	 * Get the {@link EventDeserializer} for the given event
	 * @param Event
	 * @return {@link EventDeserializer}
	 */
	@Override
	public EventDeserializer getEventDeserializer(EventTypeId eventTypeId) {
		return eventResolver.wrapDeserializer(definitions.getEventDeserializer(eventTypeId));
	}

	/**
	 * Get the corresponding {@link EventField} from the eventdefinition of the given event and fieldname
	 * @param eventTypeId {@link Long} event type id
	 * @param fieldName {@link String} field name
	 * @return {@link EventField}
	 */
	@Override
	public EventField getEventField(EventTypeId eventTypeId, String fieldName) {
		return definitions.getEventField(eventTypeId, fieldName);
	}

	@Override
	public EventRepoReport report() {
		return new EventRepoReport()
			.appendLine("MappingEventDefinitions")
			.appendReport("EventResolver", eventResolver.report())
			.appendReport("Definitions", definitions.report());
	}

}
