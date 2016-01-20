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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.jsa.jles.EventStoreConfigurer.WriteStrategy;
import se.jsa.jles.FileChannelFactory;
import se.jsa.jles.internal.EntryFile;


@RunWith(value = Parameterized.class)
public class FlippingEntryFileTest extends EntryFileContract {

	private interface EntryFileFactory {
		public EntryFile create(String fileName, FileChannelFactory fileChannelFactory);
	}

	@Parameters
	public static Collection<Object[]> entryFiles() {
		return Arrays.asList(new Object[] {
				new EntryFileFactory() {
					@Override
					public EntryFile create(String fileName, FileChannelFactory fileChannelFactory) {
						return new FlippingEntryFile(fileName, fileChannelFactory, WriteStrategy.FAST);
					}
					@Override
					public String toString() { return "FlippingEntryFile, no safe writes"; }
				}
			},
			new Object[] {
				new EntryFileFactory() {
					@Override
					public EntryFile create(String fileName, FileChannelFactory fileChannelFactory) {
						return new FlippingEntryFile(fileName, fileChannelFactory, WriteStrategy.SAFE);
					}
					@Override
					public String toString() { return "FlippingEntryFile, safe writes"; }
				}
			},
			new Object[] {
				new EntryFileFactory() {
					@Override
					public EntryFile create(String fileName, FileChannelFactory fileChannelFactory) {
						return new FlippingEntryFile(fileName, fileChannelFactory, WriteStrategy.SUPERSAFE);
					}
					@Override
					public String toString() { return "FlippingEntryFile, supersafe writes"; }
				}
			});
	}

	private final StreamBasedChannelFactory fileChannelFactory = new StreamBasedChannelFactory();
	private final EntryFileFactory entryFileFactory;

	public FlippingEntryFileTest(EntryFileFactory entryFileFactory) {
		this.entryFileFactory = entryFileFactory;
	}

	@Override
	public EntryFile createEntryFile() {
		deleteFile();
		return entryFileFactory.create("testEntryFile.ef", fileChannelFactory);
	}

	@Override
	public void closeEntryFile(EntryFile entryFile) {
		fileChannelFactory.close();
		entryFile.close();
		deleteFile();
	}

	private void deleteFile() {
		File file = new File("testEntryFile.ef");
		while (file.exists()) {
			file.delete();
		}
	}

}
