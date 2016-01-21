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

class NotRequirement extends Requirement {

	private final Requirement requirement;

	public NotRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	@Override
	public FieldConstraint createFieldContraint() {
		FieldConstraint fieldContraint = requirement.createFieldContraint();
		return FieldConstraint.create(fieldContraint.getFieldName(), new NotConstraint(fieldContraint.getConstraint()));
	}

	private static class NotConstraint extends Constraint {
		private final Constraint constraint;

		public NotConstraint(Constraint constraint) {
			this.constraint = constraint;
		}

		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return !constraint.isSatisfiedBy(eventFieldValue);
		}

		@Override
		public Class<?> getFieldType() {
			return constraint.getFieldType();
		}
	}

	@Override
	public String toString() {
		return "!" + requirement.toString();
	}

}
