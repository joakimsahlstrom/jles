package se.jsa.jles.internal;

import se.jsa.jles.internal.util.Objects;

public class EventFieldConstraint {

	private final String fieldName;
	private final FieldConstraint comparison;

	EventFieldConstraint() {
		this.fieldName = null;
		this.comparison = null;
	}

	EventFieldConstraint(String fieldName, FieldConstraint comparison) {
		this.fieldName = Objects.requireNonNull(fieldName);
		this.comparison = Objects.requireNonNull(comparison);
	}

	public static EventFieldConstraint none() {
		return new EventFieldConstraint();
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

	public FieldConstraint getComparison() {
		return comparison;
	}

	public boolean accepts(Object eventFieldValue) {
		return comparison.isSatisfied(eventFieldValue);
	}
}
