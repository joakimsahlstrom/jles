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
package se.jsa.jles.internal.fields;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import se.jsa.jles.FieldMapping;
import se.jsa.jles.FieldValueMapper;

public abstract class EventField extends StorableField implements Comparable<EventField> {
	private final Method getMethod;
	private final Method setMethod;
	private final String propertyName;
	private final String fieldName;
	private final FieldValueMapper fieldValueMapper;

	EventField(Class<?> eventType, String propertyName) {
		this.propertyName = propertyName;
		try {
			getMethod = eventType.getMethod("get" + propertyName);
			setMethod = eventType.getMethod("set" + propertyName, getFieldType());

			FieldMapping fieldMapping = getMethod.getAnnotation(FieldMapping.class);
			this.fieldName = fieldMapping != null ? fieldMapping.value() : null;
			this.fieldValueMapper = fieldMapping != null ? fieldMapping.mapper().newInstance() : new FieldValueMapper.IdentityMapper();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object getValue(Object event) {
		try {
			return getMethod.invoke(event);
		} catch (Exception e) {
			throw new RuntimeException("getMethod=" + getMethod + ", event=" + event, e);
		}
	}

	void setValue(Object event, Object value) {
		try {
			setMethod.invoke(event, value);
		} catch (Exception e) {
			throw new RuntimeException("setMethod=" + setMethod + ", event=" + event, e);
		}
	}

	public Object mapFieldValue(Object fieldValue) {
		return fieldValueMapper.map(fieldValue);
	}

	public boolean matchesFieldName(String fieldName) {
		return this.fieldName != null ? this.fieldName.equals(fieldName) : propertyName.equals(fieldName);
	}

	public void readFromBuffer(Object event, ByteBuffer buffer) {
		setValue(event, readFromBuffer(buffer));
	}

	public int getDefinitionLength(FieldSerializer fieldSerializer) {
		return fieldSerializer.getSerializedStringLength(getFieldType().getName())
				+ fieldSerializer.getSerializedStringLength(getMethod.getName().substring(3));
	}

	public void writeDefinition(ByteBuffer output, FieldSerializer fieldSerializer) {
		fieldSerializer.putString(output, getFieldType().getName());
		fieldSerializer.putString(output, getMethod.getName().substring(3));
	}

	public String getPropertyName() {
		return propertyName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public final Class<?> getFieldType() {
		return getMethod.getReturnType();
	}

	@Override
	public boolean isNullable() {
		return !getFieldType().isPrimitive();
	}

	@Override
	public boolean isNull(Object event) {
		return getValue(event) == null;
	}

	@Override
	public int compareTo(EventField o) {
		return propertyName.compareTo(o.propertyName);
	}


	@Override
	public int hashCode() {
		return getMethod.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventField) {
			EventField other = (EventField) obj;
			return getMethod.equals(other.getMethod)
					&& setMethod.equals(other.setMethod);
		}
		return false;
	}

	@Override
	public String toString() {
		return getMethod.getDeclaringClass().getSimpleName() + "." + getMethod.getName().substring(3) + "[" + getMethod.getReturnType().getSimpleName() + "]";
	}

}
