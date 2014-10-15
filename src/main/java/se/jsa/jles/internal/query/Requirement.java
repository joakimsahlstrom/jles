package se.jsa.jles.internal.query;

import java.lang.reflect.Method;

import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Primitives;
import se.jsa.jles.internal.util.ReflectionUtil;

public abstract class Requirement {
	public static Requirement NONE = new Requirement() {
		@Override
		public FieldConstraint createFieldContraint() {
			return FieldConstraint.noConstraint();
		}
	};

	public abstract FieldConstraint createFieldContraint();
	
	protected void validateFieldType(Class<?> eventType, String fieldName, Class<?> compareToType) {
		try {
			Method method = ReflectionUtil.getPropertyRetrieveMethod(eventType, fieldName);
			if (!method.getReturnType().equals(compareToType) && !Primitives.asBoxedPrimitive(method.getReturnType()).equals(compareToType)) {
				throw new IllegalArgumentException("Event of type " + eventType + " for field " + fieldName + ": " + compareToType.getSimpleName() + " not compitable with " + method.getReturnType());
			} else {
				return; // validated
			}
		} catch (NoSuchMethodError e) {
			throw new IllegalArgumentException("Could not find field " + fieldName + " for type " + eventType, e);
		}
	}
}
