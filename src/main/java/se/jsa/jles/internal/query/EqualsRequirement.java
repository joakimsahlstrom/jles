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
package se.jsa.jles.internal.query;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Objects;

class EqualsRequirement extends Requirement {
	private final String fieldName;
	private final Object equality;

	public EqualsRequirement(Class<?> eventType, String fieldName, Object equality) {
		if (equality != null) {
			validateFieldType(eventType, fieldName, equality.getClass());
		}
		this.fieldName = Objects.requireNonNull(fieldName);
		this.equality = equality;
	}

	@Override
	public FieldConstraint createFieldConstraint() {
		return FieldConstraint.create(fieldName, new EqualsConstraint(equality));
	}

	private static class EqualsConstraint extends Constraint {
		private final Object equality;

		public EqualsConstraint(Object equality) {
			this.equality = equality;
		}

		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return equality == null ? eventFieldValue == null : eventFieldValue.equals(equality);
		}

		@Override
		protected Object cast(Object eventFieldValue) {
			if (equality == null) {
				return eventFieldValue;
			} else {
				return super.cast(eventFieldValue);
			}
		}

		@Override
		public Class<?> getFieldType() {
			return equality.getClass();
		}
	}

	@Override
	public String toString() {
		return fieldName + "=" + equality;
	}
}
