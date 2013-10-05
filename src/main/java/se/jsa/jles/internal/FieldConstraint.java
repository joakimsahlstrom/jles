package se.jsa.jles.internal;

public abstract class FieldConstraint<T> {
	protected abstract boolean isSatisfied(T eventFieldValue);
	protected abstract Class<T> getFieldType();

	public final boolean isSatisfiedBy(Object eventFieldValue) {
		return isSatisfied(getFieldType().cast(eventFieldValue));
	}
}
