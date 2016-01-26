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

import java.math.BigDecimal;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Objects;

class GreaterThanRequirement extends Requirement {
	private final String fieldName;
	private final Number number;

	public GreaterThanRequirement(Class<?> eventType, String fieldName, Number number) {
		validateFieldType(eventType, fieldName, number.getClass());
		this.fieldName = Objects.requireNonNull(fieldName);
		this.number = Objects.requireNonNull(number);
	}

	@Override
	public FieldConstraint createFieldConstraint() {
		return FieldConstraint.create(fieldName, new GreaterThanConstraint(number));
	}

	private static class GreaterThanConstraint extends Constraint {
		private final Number number;

		public GreaterThanConstraint(Number number) {
			this.number = number;
		}

		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			// TODO: This is SLOW! Improve!
			return new BigDecimal(number.toString()).compareTo(new BigDecimal(eventFieldValue.toString())) < 0;
		}

		@Override
		public Class<?> getFieldType() {
			return number.getClass();
		}
	}

	@Override
	public String toString() {
		return fieldName + ">" + number;
	}
}
