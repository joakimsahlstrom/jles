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

public class FieldSerializer {

	public int getSerializedStringLength(String string) {
		return 2 + string.getBytes().length;
	}

	public void putString(ByteBuffer output, String string) {
		if (string.getBytes().length > Short.MAX_VALUE) {
			throw new RuntimeException("String was " + string.length() + " bytes, max allowed is " + Short.MAX_VALUE);
		}
		output.putShort((short)string.getBytes().length);
		output.put(string.getBytes());
	}

	public String getString(ByteBuffer input) {
		short stringLength = input.getShort();
		byte[] stringData = new byte[stringLength];
		input.get(stringData);

		return new String(stringData);
	}

}
