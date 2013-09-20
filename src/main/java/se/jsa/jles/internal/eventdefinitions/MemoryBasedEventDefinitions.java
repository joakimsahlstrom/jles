package se.jsa.jles.internal.eventdefinitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.EventFieldFactory;

public class MemoryBasedEventDefinitions implements EventDefinitions {

	enum Flag {
		VerifyDatamodel
	}

	public interface EventDefinitionsListener {
		void onNewEventDefinition(EventDefinition eventDefinition);
	}

	private final EventFieldFactory eventFieldFactory = new EventFieldFactory();
	private final Map<Class<?>, EventDefinition> eventDefinitionsByType;
	private final Map<Long, EventDefinition> eventDefinitionsById;
	private final List<EventDefinitionsListener> listeners = new ArrayList<EventDefinitionsListener>();

	public MemoryBasedEventDefinitions() {
		this(new HashMap<Class<?>, EventDefinition>(), new HashMap<Long, EventDefinition>());
	}

	/*package*/ MemoryBasedEventDefinitions(Collection<EventDefinition> definitions) {
		this();
		init(definitions, Collections.<Flag>emptySet());
	}


	@Override
	public void init() {
		// do nothing
	}

	public void init(Collection<EventDefinition> definitions, Set<Flag> flags) {
		for (EventDefinition ed : definitions) {
			if (flags.contains(Flag.VerifyDatamodel)) {
				verifyDefinition(ed);
			}
			registerEventDefinition(ed);
		}
	}

	private void verifyDefinition(EventDefinition ed) {
		List<EventField> eventFields = eventFieldFactory.fromEventType(ed.getEventType());
		if (!eventFields.equals(ed.getFields())) {
			throw new RuntimeException("Runtime definition of events does not correspond to stored definitions. Type: " + ed.getEventType().getName()
					+ " stored: " + ed.getFields()
					+ " runtime: " + eventFields);
		}
	}

	private MemoryBasedEventDefinitions(Map<Class<?>, EventDefinition> eventDefinitions, Map<Long, EventDefinition> eventDefinitionsById) {
		this.eventDefinitionsByType = eventDefinitions;
		this.eventDefinitionsById = eventDefinitionsById;
	}

	@Override
	public Class<?>[] getRegisteredEventTypes() {
		return eventDefinitionsByType.keySet().toArray(new Class<?>[eventDefinitionsByType.size()]);
	}

	@Override
	public EventSerializer getEventSerializer(Object event) {
		registerEventType(event.getClass());
		return eventDefinitionsByType.get(event.getClass());
	}

	@Override
	public EventDeserializer getEventDeserializer(Long eventTypeId) {
		if (!eventDefinitionsById.containsKey(eventTypeId)) {
			throw new RuntimeException("Unknown event type id: " + eventTypeId);
		}
		return eventDefinitionsById.get(eventTypeId);
	}

	@Override
	public Set<Long> getEventTypeIds(Class<?>... eventTypes) {
		if (eventTypes.length == 0) {
			return eventDefinitionsById.keySet();
		}

		HashSet<Long> result = new HashSet<Long>();
		 for (Class<?> eventType : eventTypes) {
			 registerEventType(eventType);
			 result.add(eventDefinitionsByType.get(eventType).getEventTypeId());
		 }
		return result;
	}

	@Override
	public EventField getEventField(Long eventTypeId, String fieldName) {
		return eventDefinitionsById.get(eventTypeId).getField(fieldName);
	}

	public void addListener(EventDefinitionsListener listener) {
		this.listeners.add(listener);
	}

	private void registerEventType(Class<?> eventType) {
		if (!eventDefinitionsByType.containsKey(eventType)) {
			EventDefinition eventDefinition = new EventDefinition(
					(long) eventDefinitionsByType.size(),
					eventType,
					eventFieldFactory.fromEventType(eventType));
			registerEventDefinition(eventDefinition);
		}
	}

	private void registerEventDefinition(EventDefinition eventDefinition) {
		eventDefinitionsByType.put(eventDefinition.getEventType(), eventDefinition);
		eventDefinitionsById.put(eventDefinition.getEventTypeId(), eventDefinition);

		for (EventDefinitionsListener listener : listeners) {
			listener.onNewEventDefinition(eventDefinition);
		}
	}

}