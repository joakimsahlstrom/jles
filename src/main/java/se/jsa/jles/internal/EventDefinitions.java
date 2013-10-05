package se.jsa.jles.internal;

import java.util.Set;

import se.jsa.jles.internal.fields.EventField;

public interface EventDefinitions {

	void init();
	void close();

	Class<?>[] getRegisteredEventTypes();

	Set<Long> getEventTypeIds(Class<?>... eventTypes);

	EventSerializer getEventSerializer(Object event);

	EventDeserializer getEventDeserializer(Long eventTypeId);

	EventField getEventField(Long eventTypeId, String fieldName);

}