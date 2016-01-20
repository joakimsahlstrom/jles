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
import java.util.WeakHashMap;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.util.Objects;

public class CachingEntryFile implements EntryFile {

	private final EntryFile file;
	private final WeakHashMap<Long, ByteBuffer> cache = new WeakHashMap<Long, ByteBuffer>();

	public CachingEntryFile(EntryFile file) {
		this.file = Objects.requireNonNull(file);
	}

	@Override
	public long append(ByteBuffer data) {
		return file.append(data);
	}

	@Override
	public ByteBuffer readEntry(long position) {
		ByteBuffer result = cache.get(position);
		if (result != null) {
			result.rewind();
			return result;
		}

		result = file.readEntry(position);
		cache.put(position, result);
		return result;
	}

	@Override
	public long size() {
		return file.size();
	}

	@Override
	public void close() {
		file.close();
	}

}