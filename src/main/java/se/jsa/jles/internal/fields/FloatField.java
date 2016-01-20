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

import java.nio.ByteBuffer;

public class FloatField extends EventField {

	public FloatField(Class<?> eventType, String propertyName) {
		super(eventType, propertyName);
	}

	@Override
	public int getSize(Object event) {
		return 4;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		buffer.putFloat(Float.class.cast(getValue(event)));
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		return buffer.getFloat();
	}

}
