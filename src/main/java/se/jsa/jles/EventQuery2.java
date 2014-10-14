package se.jsa.jles;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;

public final class EventQuery2 {

	private Class<?> eventType;
	private Requirement requirement;
	private EventQuery2 next;

	private EventQuery2(Class<?> eventType, Requirement requirement, EventQuery2 next) {
		this.eventType = Objects.requireNonNull(eventType);
		this.requirement = requirement;
		this.next = next;
	}

	public static EventQuery2 select(Class<?> eventType) {
		return new EventQuery2(eventType, Requirement.NONE, null);
	}

	public Class<?> getEventType() {
		return eventType;
	}

	public EventQueryByBuilder where(String fieldName) {
		return new EventQueryByBuilder(this, fieldName);
	}
	
	public EventQuery2 join(Class<?> eventType) {
		return new EventQuery2(eventType, Requirement.NONE, this);
	}
	
	private EventQuery2 withRequirement(Requirement requirement) {
		return new EventQuery2(eventType, requirement, this.next);
	}
	
	EventQuery2 next() {
		return next;
	}

	FieldConstraint createFieldConstraint() {
		return requirement.createFieldContraint();
	}
	
	public static class EventQueryByBuilder {
		private EventQuery2 sourceQuery;
		private String fieldName;

		public EventQueryByBuilder(EventQuery2 sourceQuery, String fieldName) {
			validateFieldName(sourceQuery.getEventType(), fieldName);
			this.sourceQuery = sourceQuery;
			this.fieldName = fieldName;
		}

		private void validateFieldName(Class<?> eventType, String fieldName) {
			for (Method method : eventType.getMethods()) {
				if ((method.getName().equals("get" + fieldName) || method.getName().equals("has" + fieldName) || method.getName().equals("is" + fieldName))
						&& method.getParameterTypes().length == 0) {
					return;
				}
			}
			throw new IllegalArgumentException("Could not find field " + fieldName + " for type " + eventType);
		}

		public EventQuery2 is(Object equality) {
			return sourceQuery.withRequirement(new EqualsRequirement(sourceQuery.getEventType(), fieldName, equality));
		}
	}
	
	private interface Requirement {
		Requirement NONE = new Requirement() {
			@Override
			public FieldConstraint createFieldContraint() {
				return FieldConstraint.noConstraint();
			}
		};

		FieldConstraint createFieldContraint();
	}
	
	private static class EqualsRequirement implements Requirement {
		private String fieldName;
		private Object equality;

		public EqualsRequirement(Class<?> eventType, String fieldName, Object equality) {
			validateFieldType(eventType, fieldName, equality.getClass());
			this.fieldName = Objects.requireNonNull(fieldName);
			this.equality = Objects.requireNonNull(equality);
		}
		
		private void validateFieldType(Class<?> eventType, String fieldName, Class<?> equalityType) {
			for (Method method : eventType.getMethods()) {
				if ((method.getName().equals("get" + fieldName) || method.getName().equals("has" + fieldName) || method.getName().equals("is" + fieldName))
						&& method.getParameterTypes().length == 0) {
					if (!method.getReturnType().equals(equalityType) && !asBoxedPrimitive(method.getReturnType()).equals(equalityType)) {
						throw new IllegalArgumentException("Event of type " + eventType + " for field " + fieldName + ": " + equalityType.getSimpleName() + " not compitable with " + method.getReturnType());
					} else {
						return; // validated
					}
				}
			}
			throw new IllegalArgumentException("Could not find field " + fieldName + " for type " + eventType);
		}
		
		public final static Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
		static {
		    map.put(boolean.class, Boolean.class);
		    map.put(byte.class, Byte.class);
		    map.put(short.class, Short.class);
		    map.put(char.class, Character.class);
		    map.put(int.class, Integer.class);
		    map.put(long.class, Long.class);
		    map.put(float.class, Float.class);
		    map.put(double.class, Double.class);
		}
		private Object asBoxedPrimitive(Class<?> returnType) {
			if (returnType.isPrimitive()) {
				return map.get(returnType);
			}
			return returnType;
		}

		@Override
		public FieldConstraint createFieldContraint() {
			return FieldConstraint.create(fieldName, new Constraint() {
				@Override
				protected boolean isSatisfied(Object eventFieldValue) {
					return equality == null ? eventFieldValue == null : eventFieldValue.equals(equality);
				}

				@Override
				protected Class<?> getFieldType() {
					return equality.getClass();
				}
			});
		}
	}
}
