package se.jsa.jles.internal.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Objects;
import se.jsa.jles.internal.util.ReflectionUtil;

public class InRequirement extends Requirement {
	private String fieldName;
	private Object[] equalities;
	private Class<?> fieldType;

	public InRequirement(Class<?> eventType, String fieldName, Object[] equalities) {
		for (Object o : equalities) {
			validateFieldType(eventType, fieldName, o.getClass());
		}
		this.fieldName = Objects.requireNonNull(fieldName);
		this.equalities = equalities;
		this.fieldType = ReflectionUtil.getPropertyRetrieveMethod(eventType, fieldName).getReturnType();
	}

	@Override
	public FieldConstraint createFieldContraint() {
		return FieldConstraint.create(fieldName, new InConstraint(equalities == null ? null : new HashSet<Object>(Arrays.asList(equalities)), fieldType));
	}
	
	private static class InConstraint extends Constraint {
		private Set<Object> equality;
		private Class<?> fieldType;

		public InConstraint(Set<Object> equality, Class<?> fieldType) {
			this.equality = equality;
			this.fieldType = fieldType;
		}
		
		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return equality == null ? eventFieldValue == null : equality.contains(eventFieldValue);
		}

		@Override
		public Class<?> getFieldType() {
			return fieldType;
		}
	}


}
