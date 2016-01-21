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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Objects;
import se.jsa.jles.internal.util.ReflectionUtil;

public class InRequirement extends Requirement {
	private final String fieldName;
	private final Object[] equalities;
	private final Class<?> fieldType;

	public InRequirement(Class<?> eventType, String fieldName, Object[] equalities) {
		for (Object o : equalities) {
			validateFieldType(eventType, fieldName, o.getClass());
		}
		this.fieldName = Objects.requireNonNull(fieldName);
		this.equalities = equalities;
		this.fieldType = ReflectionUtil.getPropertyRetrieveMethod(eventType, fieldName).getReturnType();
	}

	@Override
	public FieldConstraint createFieldContraint() {
		return FieldConstraint.create(fieldName, new InConstraint(equalities == null ? null : new HashSet<Object>(Arrays.asList(equalities)), fieldType));
	}

	private static class InConstraint extends Constraint {
		private final Set<Object> equality;
		private final Class<?> fieldType;

		public InConstraint(Set<Object> equality, Class<?> fieldType) {
			this.equality = equality;
			this.fieldType = fieldType;
		}

		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return equality == null ? eventFieldValue == null : equality.contains(eventFieldValue);
		}

		@Override
		public Class<?> getFieldType() {
			return fieldType;
		}
	}

	@Override
	public String toString() {
		return fieldName + " in (" + Arrays.asList(equalities) + ")";
	}

}
