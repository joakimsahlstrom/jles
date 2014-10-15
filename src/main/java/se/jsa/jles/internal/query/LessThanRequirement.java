package se.jsa.jles.internal.query;

import java.math.BigDecimal;
import java.util.Objects;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;

public class LessThanRequirement extends Requirement {

	private final String fieldName;
	private final Number number;
	
	public LessThanRequirement(Class<?> eventType, String fieldName, Number number) {
		validateFieldType(eventType, fieldName, number.getClass());
		this.fieldName = Objects.requireNonNull(fieldName);
		this.number = Objects.requireNonNull(number);
	}
	
	@Override
	public FieldConstraint createFieldContraint() {
		return FieldConstraint.create(fieldName, new LessThanConstraint(number));
	}
	
	private static class LessThanConstraint extends Constraint {
		private Number number;

		public LessThanConstraint(Number number) {
			this.number = number;
		}
		
		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			// TODO: This is SLOW! Improve!
			return new BigDecimal(number.toString()).compareTo(new BigDecimal(eventFieldValue.toString())) > 0;
		}

		@Override
		public Class<?> getFieldType() {
			return number.getClass();
		}
	}

}
