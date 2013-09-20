package se.jsa.jles.internal.eventdefinitions;

import se.jsa.jles.internal.EventDeserializer;
import se.jsa.jles.internal.EventSerializer;

public interface EventResolver {

	public void registerEventTypes(Class<?>[] eventTypes);

	/**
	 * Try to find a corresponding serializable event by some strategy. If that fails,
	 * return the incoming object
	 */
	public Object getSerializableEvent(Object event);

	/**
	 * @param eventTypes
	 * @return All types that are serializable versions of the incoming types
	 */
	public Class<?>[] getSerializableEventTypes(Class<?>[] eventTypes);

	public EventDeserializer wrapDeserializer(EventDeserializer eventDeserializer);

	public EventSerializer wrapSerializer(EventSerializer eventSerializer);

}