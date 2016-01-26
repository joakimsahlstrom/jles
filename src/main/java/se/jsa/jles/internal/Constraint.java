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

public abstract class Constraint {
	protected abstract boolean isSatisfied(Object eventFieldValue);
	public abstract Class<?> getFieldType();

	public final boolean isSatisfiedBy(Object eventFieldValue) {
		try {
			return isSatisfied(cast(eventFieldValue));
		} catch (ClassCastException e) {
			throw new ClassCastException("Could not cast " + eventFieldValue.getClass() + " to " + getFieldType());
		}
	}

	protected Object cast(Object eventFieldValue) {
		return getFieldType().cast(eventFieldValue);
	}
}
