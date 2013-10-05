package se.jsa.jles.internal;

import se.jsa.jles.internal.util.Objects;

public class EventFieldConstraint {
	private final String fieldName;
	private final FieldConstraint<?> constraint;

	EventFieldConstraint() {
		this.fieldName = null;
		this.constraint = null;
	}

	EventFieldConstraint(String fieldName, FieldConstraint<?> constraint) {
		this.fieldName = Objects.requireNonNull(fieldName);
		this.constraint = Objects.requireNonNull(constraint);
	}

	public static EventFieldConstraint none() {
		return new EventFieldConstraint();
	}

	public static EventFieldConstraint create(String fieldName, FieldConstraint<?> constraint) {
		return new EventFieldConstraint(fieldName, constraint);
	}

	public boolean hasConstraint() {
		return fieldName != null;
	}

	public String getFieldName() {
		if (fieldName == null) {
			throw new NullPointerException("No constraint set");
		}
		return fieldName;
	}

	public boolean accepts(Object eventFieldValue) {
		return constraint.isSatisfiedBy(eventFieldValue);
	}
}
