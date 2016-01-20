package se.jsa.jles.configuration;

import se.jsa.jles.EventStoreConfigurer.EventFieldIndexConfiguration;
import se.jsa.jles.internal.EventDefinitions;
import se.jsa.jles.internal.EventFieldIndex;
import se.jsa.jles.internal.EventFieldIndex.EventFieldId;
import se.jsa.jles.internal.EventIndexPreparation;
import se.jsa.jles.internal.EventTypeId;
import se.jsa.jles.internal.InMemoryEventFieldIndex;
import se.jsa.jles.internal.SimpleEventFieldIndex;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.util.Objects;

public class EventFieldIndexFactory {

	private final EventDefinitions eventDefinitions;
	private final EventIndexPreparation preparation;
	private final EntryFileNameGenerator entryFileNameGenerator;
	private final EntryFileFactory entryFileFactory;

	public EventFieldIndexFactory(EventDefinitions eventDefinitions,
			EventIndexPreparation preparation,
			EntryFileNameGenerator entryFileNameGenerator,
			EntryFileFactory entryFileFactory) {
		this.entryFileFactory = entryFileFactory;
		this.eventDefinitions = Objects.requireNonNull(eventDefinitions);
		this.preparation = Objects.requireNonNull(preparation);
		this.entryFileNameGenerator = Objects.requireNonNull(entryFileNameGenerator);
	}

	public EventFieldIndex createEventFieldIndex(EventFieldIndexConfiguration eventFieldIndexConfiguration, EventTypeId eventTypeId) {
		EventFieldIndex eventFieldIndex = createEventFieldIndex(
				eventFieldIndexConfiguration,
				eventTypeId,
				eventDefinitions.getEventField(eventTypeId, eventFieldIndexConfiguration.getFieldName()));
		eventFieldIndex.prepare(preparation);
		return eventFieldIndex;
	}

	private EventFieldIndex createEventFieldIndex(EventFieldIndexConfiguration eventFieldIndexConfiguration, EventTypeId eventTypeId, EventField eventField) {
		if (eventFieldIndexConfiguration.inMemory()) {
			return new InMemoryEventFieldIndex(
					new EventFieldId(eventTypeId, eventField.getPropertyName()),
					eventField,
					preparation.getEventTypeIndex(),
					eventDefinitions,
					preparation.getEventFile());
		} else {
			return new SimpleEventFieldIndex(
					eventTypeId,
					eventField,
					entryFileFactory.createEntryFile(entryFileNameGenerator.getEventFieldIndexFileName(eventTypeId, eventFieldIndexConfiguration.getFieldName())));
		}
	}

}
