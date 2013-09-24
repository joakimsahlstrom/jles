package se.jsa.jles.internal.eventdefinitions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.fields.FieldSerializer;
import se.jsa.jles.internal.util.Objects;

/**
 * eventTypeId(8):eventDefinitionLength(4):eventDefinition(eventDefinitionLength)
 *
 * where eventDefinition:
 * String eventTypeName
 * int numFields
 * for numFields:
 *   String fieldType
 *   String fieldName
 *
 * @author joakim
 *
 */
@SuppressWarnings("unchecked")
public class EventDefinitionFile {

	private final FieldSerializer fieldSerializer = new FieldSerializer();
	private final EventFieldFactory eventFieldFactory = new EventFieldFactory();
	private final EntryFile entryFile;

	public EventDefinitionFile(EntryFile entryFile) {
		this.entryFile = Objects.requireNonNull(entryFile);
	}

	public Collection<EventDefinition> readAllEventDefinitions() {
		long position = 0;
		ArrayList<EventDefinition> result = new ArrayList<EventDefinition>();
		while (position < entryFile.size()) {
			ByteBuffer entry = entryFile.readEntry(position);
			try {
				result.add(readEventDefinition(entry));
			} catch (ClassNotFoundException e) {
				entry.rewind();
				throw new RuntimeException("Unknown field definition: " + entry, e);
			}
			position += entry.limit();
		}
		return result;
	}

	private EventDefinition readEventDefinition(ByteBuffer entry) throws ClassNotFoundException {
		Long eventTypeId = entry.getLong();
		@SuppressWarnings("unused") int eventDefinitionLength = entry.getInt();
		String eventTypeName = fieldSerializer.getString(entry);
		Class<?> eventType = EventDefinitionFile.class.getClassLoader().loadClass(eventTypeName);
		int numFields = entry.getInt();
		List<EventField> fields = new ArrayList<EventField>(numFields);
		for (int i = 0; i < numFields; i++) {
			String fieldTypeName = fieldSerializer.getString(entry);
			String fieldName = fieldSerializer.getString(entry);
			Class<?> fieldType = loadClass(fieldTypeName);
			fields.add(eventFieldFactory.createEventField(fieldType, fieldName, eventType));
		}
		return new EventDefinition(eventTypeId, eventType, fields);
	}

	public void write(EventDefinition eventDefinition) {
		ByteBuffer output = eventDefinition.toEventFileEntry(fieldSerializer);
		entryFile.append(output);
	}

	private Class<?> loadClass(String fieldTypeName) {
		if (primitiveTypeNameMap.containsKey(fieldTypeName)) {
			return primitiveTypeNameMap.get(fieldTypeName);
		}
		if (fieldTypeName.equals(String.class.getName())) {
			return String.class;
		}
		throw new RuntimeException("Unknown field type name: " + fieldTypeName);
	}

	private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(16);
	// and populate like this
	static {
		List<Class<?>> primitiveTypeNames = new ArrayList<Class<?>>();
		primitiveTypeNames.addAll(Arrays.asList(
	        boolean.class, byte.class, char.class, double.class,
	        float.class, int.class, long.class, short.class));
		for (Class<?> primitiveClass : primitiveTypeNames) {
			primitiveTypeNameMap.put(primitiveClass.getName(), primitiveClass);
		}
	}

	public void close() {
		entryFile.close();
	}

}
