package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventFieldFactory {

	private class NameTypePair {
		private final String name;
		private final Class<?> type;

		public NameTypePair(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public int hashCode() {
			return name.hashCode() * 31 + type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NameTypePair) {
				NameTypePair other = (NameTypePair) obj;
				return name.equals(other.name) && type.equals(other.type);
			}
			return false;
		}

		public EventField createEventField(Class<?> eventType) {
			return EventFieldFactory.this.createEventField(type, name, eventType);
		}

		@Override
		public String toString() {
			return name + ":" + type;
		}
	}

	public List<EventField> fromEventType(Class<?> eventType) {
		Method[] methods = eventType.getMethods();
		Set<NameTypePair> getMethods = extractGetMethod(methods);
		Set<NameTypePair> setMethods = extractSetMethod(methods);
		if (!getMethods.equals(setMethods)) {
			throw new IllegalArgumentException("Given class " + eventType.getSimpleName() + " is not a valid event class. Getters and setters does not match. Getters: "+ getMethods + " setters: " + setMethods);
		}

		ArrayList<EventField> result = new ArrayList<EventField>(getMethods.size());
		for (NameTypePair ntp : getMethods) {
			result.add(ntp.createEventField(eventType));
		}
		Collections.sort(result);
		return result;
	}

	public EventField createEventField(Class<?> fieldType, String name, Class<?> eventType) {
		if (fieldType.equals(Boolean.TYPE) || fieldType.equals(Boolean.class)) {
			return new BooleanField(eventType, name);
		} else if (fieldType.equals(Integer.TYPE) || fieldType.equals(Integer.class)) {
			return new IntegerField(eventType, name);
		} else if (fieldType.equals(String.class)) {
			return new StringField(eventType, name);
		} else if (fieldType.equals(Byte.TYPE) || fieldType.equals(Byte.class)) {
			return new ByteField(eventType, name);
		} else if (fieldType.equals(Short.TYPE) || fieldType.equals(Short.class)) {
			return new ShortField(eventType, name);
		} else if (fieldType.equals(Long.TYPE) || fieldType.equals(Long.class)) {
			return new LongField(eventType, name);
		} else if (fieldType.equals(Float.TYPE) || fieldType.equals(Float.class)) {
			return new FloatField(eventType, name);
		} else if (fieldType.equals(Double.TYPE) || fieldType.equals(Double.class)) {
			return new DoubleField(eventType, name);
		} else if (fieldType.equals(Character.TYPE) || fieldType.equals(Character.class)) {
			return new CharField(eventType, name);
		}
		throw new RuntimeException("Cannot create EventField for type " + fieldType);
	}

	private Set<NameTypePair> extractGetMethod(Method[] methods) {
		HashSet<NameTypePair> result = new HashSet<NameTypePair>();
		for (Method m : methods) {
			if ((m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC
					&& m.getName().startsWith("get")
					&& m.getName().length() > 3
					&& m.getParameterTypes().length == 0
					&& !isExcluded(m.getName())) {
				result.add(new NameTypePair(m.getName().substring(3), m.getReturnType()));
			}
		}
		return result;
	}

	private Set<NameTypePair> extractSetMethod(Method[] methods) {
		HashSet<NameTypePair> result = new HashSet<NameTypePair>();
		for (Method m : methods) {
			if ((m.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC
					&& m.getName().startsWith("set")
					&& m.getName().length() > 3
					&& m.getReturnType().equals(Void.TYPE)
					&& m.getParameterTypes().length == 1
					&& !isExcluded(m.getName())) {
				result.add(new NameTypePair(m.getName().substring(3), m.getParameterTypes()[0]));
			}
		}
		return result;
	}

	private boolean isExcluded(String name) {
		return name.substring(3).equals("Class");
	}

}
