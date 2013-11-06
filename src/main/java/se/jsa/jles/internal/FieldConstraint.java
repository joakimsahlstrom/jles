package se.jsa.jles.internal;

import se.jsa.jles.internal.util.Objects;

//TODO: move fieldName to FieldConstraint and delete this file?
public class FieldConstraint {
	private final String fieldName;
	private final Constraint<?> constraint;

	FieldConstraint() {
		this.fieldName = null;
		this.constraint = null;
	}

	protected FieldConstraint(String fieldName, Constraint<?> constraint) {
		this.fieldName = Objects.requireNonNull(fieldName);
		this.constraint = Objects.requireNonNull(constraint);
	}

	public static FieldConstraint noConstraint() {
		return new FieldConstraint();
	}

	public static FieldConstraint create(String fieldName, Constraint<?> constraint) {
		return new FieldConstraint(fieldName, constraint);
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
