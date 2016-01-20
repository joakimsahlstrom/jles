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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import se.jsa.jles.internal.EntryFile;

/**
 * EntryFile
 *
 * key(8):entrySize(4):entry(entrySize)
 *
 * @author joakim
 *
 */
public class SynchronousEntryFile implements EntryFile {

	private final String fileName;
	private final EntryFileSerializer entryReader = new EntryFileSerializer();

	public SynchronousEntryFile(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public long append(ByteBuffer data) {
		FileChannel channel = null;
		try {
			channel = new FileOutputStream(fileName, true).getChannel();
			return entryReader.append(data, channel);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public ByteBuffer readEntry(long position) {
		FileChannel channel = null;
		try {
			channel = new FileInputStream(fileName).getChannel();
			return entryReader.readEntry(position, channel);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public long size() {
		FileChannel channel = null;
		try {
			channel = new FileInputStream(fileName).getChannel();
			return channel.size();
		} catch (Exception e) {
			return 0;
		} finally {
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public void close() {
		// do nothing
	}

}