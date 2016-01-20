package se.jsa.jles.internal;

import java.util.Set;

import se.jsa.jles.internal.fields.EventField;

public interface EventDefinitions {

	void init();
	void close();

	Class<?>[] getRegisteredEventTypes();

	Set<EventTypeId> getEventTypeIds(Class<?>... eventTypes);

	EventSerializer getEventSerializer(Object event);

	EventDeserializer getEventDeserializer(EventTypeId eventTypeId);

	EventField getEventField(EventTypeId eventTypeId, String fieldName);

}