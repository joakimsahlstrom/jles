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

import java.lang.reflect.Method;

import se.jsa.jles.internal.FieldConstraint;
import se.jsa.jles.internal.util.Primitives;
import se.jsa.jles.internal.util.ReflectionUtil;

public abstract class Requirement {
	public static Requirement NONE = new Requirement() {
		@Override
		public FieldConstraint createFieldContraint() {
			return FieldConstraint.noConstraint();
		}

		@Override
		public String toString() {
			return "No requirement";
		}
	};

	public abstract FieldConstraint createFieldContraint();

	protected void validateFieldType(Class<?> eventType, String fieldName, Class<?> compareToType) {
		try {
			Method method = ReflectionUtil.getPropertyRetrieveMethod(eventType, fieldName);
			if (!method.getReturnType().equals(compareToType) && !Primitives.asBoxedPrimitive(method.getReturnType()).equals(compareToType)) {
				throw new IllegalArgumentException("Event of type " + eventType + " for field " + fieldName + ": " + compareToType.getSimpleName() + " not compitable with " + method.getReturnType());
			} else {
				return; // validated
			}
		} catch (NoSuchMethodError e) {
			throw new IllegalArgumentException("Could not find field " + fieldName + " for type " + eventType, e);
		}
	}

	@Override
	public abstract String toString();
}
