package se.jsa.jles.internal.query;

import java.math.BigDecimal;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Objects;

class GreaterThanRequirement extends Requirement {
	private final String fieldName;
	private final Number number;

	public GreaterThanRequirement(Class<?> eventType, String fieldName, Number number) {
		validateFieldType(eventType, fieldName, number.getClass());
		this.fieldName = Objects.requireNonNull(fieldName);
		this.number = Objects.requireNonNull(number);
	}

	@Override
	public FieldConstraint createFieldContraint() {
		return FieldConstraint.create(fieldName, new GreaterThanConstraint(number));
	}

	private static class GreaterThanConstraint extends Constraint {
		private final Number number;

		public GreaterThanConstraint(Number number) {
			this.number = number;
		}

		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			// TODO: This is SLOW! Improve!
			return new BigDecimal(number.toString()).compareTo(new BigDecimal(eventFieldValue.toString())) < 0;
		}

		@Override
		public Class<?> getFieldType() {
			return number.getClass();
		}
	}

}
