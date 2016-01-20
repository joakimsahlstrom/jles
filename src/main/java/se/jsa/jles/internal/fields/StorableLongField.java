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

public class StorableLongField extends StorableField {

	@Override
	public Class<?> getFieldType() {
		return Long.class;
	}

	@Override
	public int getSize(Object event) {
		return 8;
	}

	@Override
	public void writeToBuffer(Object obj, ByteBuffer buffer) {
		buffer.putLong(Long.class.cast(obj));
	}

	@Override
	public Long readFromBuffer(ByteBuffer buffer) {
		return buffer.getLong();
	}

	@Override
	public boolean isNullable() {
		return false; // This class is used for writing indexes and they are never null
	}

	@Override
	public boolean isNull(Object event) {
		return false;
	}

}
