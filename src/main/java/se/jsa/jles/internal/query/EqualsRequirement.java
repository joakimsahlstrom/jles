package se.jsa.jles.internal.query;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Objects;

class EqualsRequirement extends Requirement {
	private final String fieldName;
	private final Object equality;

	public EqualsRequirement(Class<?> eventType, String fieldName, Object equality) {
		validateFieldType(eventType, fieldName, equality.getClass());
		this.fieldName = Objects.requireNonNull(fieldName);
		this.equality = Objects.requireNonNull(equality);
	}

	@Override
	public FieldConstraint createFieldContraint() {
		return FieldConstraint.create(fieldName, new EqualsConstraint(equality));
	}

	private static class EqualsConstraint extends Constraint {
		private final Object equality;

		public EqualsConstraint(Object equality) {
			this.equality = equality;
		}

		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return equality == null ? eventFieldValue == null : eventFieldValue.equals(equality);
		}

		@Override
		public Class<?> getFieldType() {
			return equality.getClass();
		}
	}
}
