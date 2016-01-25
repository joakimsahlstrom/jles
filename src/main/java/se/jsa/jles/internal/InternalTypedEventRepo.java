package se.jsa.jles.internal;

import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class InternalTypedEventRepo implements TypedEventRepo {
	private final EventTypeId eventTypeId;
	private final EventDeserializer eventDeserializer;
	private final EventFile eventFile;
	private final EventDefinitions eventDefinitions;

	public InternalTypedEventRepo(EventTypeId eventTypeId, EventFile eventFile, EventDefinitions eventDefinitions) {
		this.eventTypeId = Objects.requireNonNull(eventTypeId);
		this.eventFile = eventFile;
		this.eventDefinitions = eventDefinitions;
		this.eventDeserializer = eventDefinitions.getEventDeserializer(eventTypeId);
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