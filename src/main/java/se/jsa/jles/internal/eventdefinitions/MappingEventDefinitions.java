package se.jsa.jles.internal.eventdefinitions;

import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class MappingEventDefinitions implements EventDefinitions {

	private final EventDefinitions definitions;
	private final EventResolver eventResolver;

	public MappingEventDefinitions(EventDefinitions definitions) {
		this(definitions, new ConventionbasedEventResolver());
	}

	MappingEventDefinitions(EventDefinitions definitions, EventResolver eventResolver) {
		this.definitions = Objects.requireNonNull(definitions);
		this.eventResolver = Objects.requireNonNull(eventResolver);

	}

	@Override
	public void init() {
		definitions.init();
		eventResolver.registerEventTypes(definitions.getRegisteredEventTypes());
	}

	@Override
	public void close() {
		definitions.close();
	}

	@Override
	public Class<?>[] getRegisteredEventTypes() {
		return definitions.getRegisteredEventTypes();
	}

	@Override
	public Set<Long> getEventTypeIds(Class<?>... eventTypes) {
		return definitions.getEventTypeIds(eventResolver.getSerializableEventTypes(eventTypes));
	}

	@Override
	public EventSerializer getEventSerializer(Object event) {
		return eventResolver.wrapSerializer(definitions.getEventSerializer(eventResolver.getSerializableEvent(event)));
	}

	@Override
	public EventDeserializer getEventDeserializer(Long eventTypeId) {
		return eventResolver.wrapDeserializer(definitions.getEventDeserializer(eventTypeId));
	}

	@Override
	public EventField getEventField(Long eventTypeId, String fieldName) {
		return definitions.getEventField(eventTypeId, fieldName);
	}

}
