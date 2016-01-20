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

public class StringField extends EventField {

	public StringField(Class<?> eventType, String value) {
		super(eventType, value);
	}

	@Override
	public int getSize(Object event) {
		return 4 + String.class.cast(getValue(event)).getBytes().length;
	}

	@Override
	public void writeToBuffer(Object event, ByteBuffer buffer) {
		String val = String.class.cast(getValue(event));
		byte[] stringData = val.getBytes();
		buffer.putInt(stringData.length);
		buffer.put(stringData);
	}

	@Override
	public Object readFromBuffer(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte[] stringData = new byte[length];
		buffer.get(stringData);

		return new String(stringData);
	}

}
