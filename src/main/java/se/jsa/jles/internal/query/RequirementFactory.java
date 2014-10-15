package se.jsa.jles.internal.query;


public class RequirementFactory {

	public Requirement createEqualsRequirement(Class<?> eventType, String fieldName, Object equality) {
		return new EqualsRequirement(eventType, fieldName, equality);
	}

	public Requirement createGreaterThanRequirement(Class<?> eventType, String fieldName, Number number) {
		return new GreaterThanRequirement(eventType, fieldName, number);
	}

	public Requirement not(Requirement requirement) {
		return new NotRequirement(requirement);
	}

	public Requirement createLessThanRequirement(Class<?> eventType, String fieldName, Number number) {
		return new LessThanRequirement(eventType, fieldName, number);
	}
	
}
