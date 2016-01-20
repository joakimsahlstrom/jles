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
package se.jsa.jles.internal.file;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.internal.EntryFile;

public class InMemoryEntryFile implements EntryFile {

	private long position = 0;
	private final Map<Long, ByteBuffer> entries = new HashMap<Long, ByteBuffer>();

	@Override
	public long append(ByteBuffer data) {
		entries.put(position, createCopy(data));
		long index = position;
		position += data.limit();
		return index;
	}

	private ByteBuffer createCopy(ByteBuffer data) {
		data.rewind();
		ByteBuffer copy = ByteBuffer.allocate(data.limit());
		copy.put(data);
		return copy;
	}

	@Override
	public ByteBuffer readEntry(long position) {
		ByteBuffer result = entries.get(position);
		result.rewind();
		return result;
	}

	@Override
	public long size() {
		return position;
	}

	@Override
	public void close() {
		// do nothing
	}

}
