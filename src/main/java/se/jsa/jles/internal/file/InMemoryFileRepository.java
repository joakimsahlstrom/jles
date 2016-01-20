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

import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.EventStoreConfigurer;
import se.jsa.jles.internal.EntryFile;

/**
 * Used for test setups (in {@link EventStoreConfigurer}) when we need to fake file persistence between event store instantiations
 */
public class InMemoryFileRepository {
	private final Map<String, EntryFile> files = new HashMap<String, EntryFile>();

	public EntryFile getEntryFile(String fileName) {
		if (!files.containsKey(fileName)) {
			files.put(fileName, new InMemoryEntryFile());
		}
		return files.get(fileName);
	}
}
