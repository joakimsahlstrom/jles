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
package se.jsa.jles.internal;

import se.jsa.jles.internal.util.Objects;

public class FieldConstraint {
	private final String fieldName;
	private final Constraint constraint;

	FieldConstraint() {
		this.fieldName = null;
		this.constraint = null;
	}

	protected FieldConstraint(String fieldName, Constraint constraint) {
		this.fieldName = Objects.requireNonNull(fieldName);
		this.constraint = Objects.requireNonNull(constraint);
	}

	public static FieldConstraint noConstraint() {
		return new FieldConstraint();
	}

	public static FieldConstraint create(String fieldName, Constraint constraint) {
		return new FieldConstraint(fieldName, constraint);
	}

	public boolean hasConstraint() {
		return constraint != null;
	}

	public String getFieldName() {
		if (fieldName == null) {
			throw new NullPointerException("No constraint set");
		}
		return fieldName;
	}

	public boolean accepts(Object eventFieldValue) {
		return constraint == null || constraint.isSatisfiedBy(eventFieldValue);
	}

	public Constraint getConstraint() {
		return constraint;
	}

}
