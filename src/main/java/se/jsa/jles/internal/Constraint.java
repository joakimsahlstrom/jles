package se.jsa.jles.internal;

public abstract class Constraint {
	protected abstract boolean isSatisfied(Object eventFieldValue);
	public abstract Class<?> getFieldType();

	public final boolean isSatisfiedBy(Object eventFieldValue) {
		try {
			return isSatisfied(getFieldType().cast(eventFieldValue));
		} catch (ClassCastException e) {
			throw new ClassCastException("Could not cast " + eventFieldValue.getClass() + " to " + getFieldType());
		}
	}
}
