package se.jsa.jles.internal.eventdefinitions;

import java.util.Collections;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class PersistingEventDefinitions implements EventDefinitions, MemoryBasedEventDefinitions.EventDefinitionsListener {

	private final EventDefinitionFile eventDefinitionFile;
	private final MemoryBasedEventDefinitions eventDefinitionCache;

	public PersistingEventDefinitions(EventDefinitionFile eventDefinitionFile) {
		this.eventDefinitionFile = Objects.requireNonNull(eventDefinitionFile);
		this.eventDefinitionCache = new MemoryBasedEventDefinitions();
	}

	@Override
	public void init() {
		eventDefinitionCache.init(eventDefinitionFile.readAllEventDefinitions(), Collections.singleton(MemoryBasedEventDefinitions.Flag.VerifyDatamodel));
		this.eventDefinitionCache.addListener(this);
	}

	@Override
	public Class<?>[] getRegisteredEventTypes() {
		return eventDefinitionCache.getRegisteredEventTypes();
	}

	@Override
	public EventDeserializer getEventDeserializer(Long eventTypeId) {
		return eventDefinitionCache.getEventDeserializer(eventTypeId);
	}

	@Override
	public EventSerializer getEventSerializer(Object event) {
		return eventDefinitionCache.getEventSerializer(event);
	}

	@Override
	public Set<Long> getEventTypeIds(Class<?>... eventTypes) {
		return eventDefinitionCache.getEventTypeIds(eventTypes);
	}

	@Override
	public EventField getEventField(Long eventTypeId, String fieldName) {
		return eventDefinitionCache.getEventField(eventTypeId, fieldName);
	}

	// ----- From MemoryBasedEventDefinitions.EventDefinitionsListener -----

	@Override
	public void onNewEventDefinition(EventDefinition eventDefinition) {
		eventDefinitionFile.write(eventDefinition);
	}

}
