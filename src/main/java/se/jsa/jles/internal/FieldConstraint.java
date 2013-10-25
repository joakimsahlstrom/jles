package se.jsa.jles.internal;

public abstract class FieldConstraint<T> {
	protected abstract boolean isSatisfied(T eventFieldValue);
	protected abstract Class<T> getFieldType();

	public final boolean isSatisfiedBy(Object eventFieldValue) {
		try {
			return isSatisfied(getFieldType().cast(eventFieldValue));
		} catch (ClassCastException e) {
			throw new ClassCastException("Could not cast " + eventFieldValue.getClass() + " to " + getFieldType());
		}
	}
}
