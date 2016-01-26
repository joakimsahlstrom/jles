/*
 * Copyright 2016 Joakim Sahlström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jsa.jles;

import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.query.Requirement;
import se.jsa.jles.internal.query.RequirementFactory;
import se.jsa.jles.internal.util.Objects;
import se.jsa.jles.internal.util.ReflectionUtil;

public final class EventQuery {

	static final RequirementFactory REQUIREMENT_FACTORY = new RequirementFactory();

	private final Class<?> eventType;
	private final Requirement requirement;
	private final EventQuery next;

	private EventQuery(Class<?> eventType, Requirement requirement, EventQuery next) {
		this.eventType = Objects.requireNonNull(eventType);
		this.requirement = Objects.requireNonNull(requirement);
		this.next = next; // may be null
	}

	// query building

	public static EventQuery select(Class<?> eventType) {
		return new EventQuery(eventType, Requirement.NONE, null);
	}

	public EventQueryWhere where(String fieldName) {
		return new EventQueryWhere(this, fieldName);
	}

	public EventQuery join(Class<?> eventType) {
		return new EventQuery(eventType, Requirement.NONE, this);
	}

	private EventQuery withRequirement(Requirement requirement) {
		return new EventQuery(eventType, requirement, this.next);
	}

	// inspection

	Class<?> getEventType() {
		return eventType;
	}

	EventQuery next() {
		return next;
	}

	FieldConstraint createFieldConstraint() {
		return requirement.createFieldConstraint();
	}

	public static class EventQueryWhere {
		private final EventQuery sourceQuery;
		private final String fieldName;

		public EventQueryWhere(EventQuery sourceQuery, String fieldName) {
			validateFieldName(sourceQuery.getEventType(), fieldName);
			this.sourceQuery = Objects.requireNonNull(sourceQuery);
			this.fieldName = Objects.requireNonNull(fieldName);
		}

		private void validateFieldName(Class<?> eventType, String fieldName) {
			try {
				ReflectionUtil.getPropertyRetrieveMethod(eventType, fieldName);
			} catch (NoSuchMethodError e) {
				throw new IllegalArgumentException("Could not find field " + fieldName + " for type " + eventType, e);
			}
		}

		public EventQuery is(Object equality) {
			return sourceQuery.withRequirement(REQUIREMENT_FACTORY.createEqualsRequirement(sourceQuery.getEventType(), fieldName, equality));
		}

		public EventQuery isGreaterThan(Number number) {
			return sourceQuery.withRequirement(REQUIREMENT_FACTORY.createGreaterThanRequirement(sourceQuery.getEventType(), fieldName, number));
		}

		public EventQuery isLessThan(Number number) {
			return sourceQuery.withRequirement(REQUIREMENT_FACTORY.createLessThanRequirement(sourceQuery.getEventType(), fieldName, number));
		}

		public EventQuery in(Object... equalities) {
			return sourceQuery.withRequirement(REQUIREMENT_FACTORY.createInRequirement(sourceQuery.getEventType(), fieldName, equalities));
		}
	}

	@Override
	public String toString() {
		return "EventQuery [eventType=" + eventType.getSimpleName() + ", requirement=" + requirement + ", next=" + next + "]";
	}

}
