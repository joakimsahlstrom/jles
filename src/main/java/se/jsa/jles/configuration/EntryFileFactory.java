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
package se.jsa.jles.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import se.jsa.jles.EventStoreConfigurer.WriteStrategy;
import se.jsa.jles.FileChannelFactory;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.file.ThreadSafeEntryFile;

public class EntryFileFactory {

	private final FileChannelFactory fileChannelFactory;
	private final InMemoryFileRepository inMemoryFileRepository;
	private final AtomicReference<ThreadingEnvironment> threadingEnvironment;

	private final List<String> files = new ArrayList<String>();
	private WriteStrategy writeStrategy = WriteStrategy.FAST;

	public EntryFileFactory(FileChannelFactory fileChannelFactory,
			InMemoryFileRepository inMemoryFileRepository,
			AtomicReference<ThreadingEnvironment> threadingEnvironment) {
		this.fileChannelFactory = fileChannelFactory;
		this.inMemoryFileRepository = inMemoryFileRepository;
		this.threadingEnvironment = threadingEnvironment;
	}

	public void setWriteStrategy(WriteStrategy writeStrategy2) {
		this.writeStrategy = writeStrategy2;
	}

	public EntryFile createEntryFile(String fileName) {
		if (fileChannelFactory == null) {
			return inMemoryFileRepository.getEntryFile(fileName);
		}

		EntryFile flippingEntryFile = new FlippingEntryFile(fileName, fileChannelFactory, writeStrategy);
		if (threadingEnvironment.get() == ThreadingEnvironment.MULTITHREADED) {
			flippingEntryFile = new ThreadSafeEntryFile(flippingEntryFile);
		}
		files.add(fileName);
		return flippingEntryFile;
	}

	public List<String> getFiles() {
		return files;
	}

}
