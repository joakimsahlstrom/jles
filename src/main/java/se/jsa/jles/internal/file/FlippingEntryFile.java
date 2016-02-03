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

import se.jsa.jles.EventStoreConfigurer.WriteStrategy;
import se.jsa.jles.FileChannelFactory;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.util.Objects;

public class FlippingEntryFile implements EntryFile {

	private final String fileName;
	private final EntryFileSerializer entryReader = new EntryFileSerializer();

	private FileInputStream inputStream = null;
	private FileChannel inputChannel = null;
	private FileOutputStream outputStream = null;
	private FileChannel outputChannel = null;
	private Long size = null;
	private FileChannelFactory fileChannelFactory;
	private final WriteStrategy writeStrategy;

	public FlippingEntryFile(String fileName, FileChannelFactory fileChannelFactory) {
		this(fileName, fileChannelFactory, WriteStrategy.FAST);
	}

	public FlippingEntryFile(String fileName, FileChannelFactory fileChannelFactory, WriteStrategy writeStrategy) {
		this.fileName = Objects.requireNonNull(fileName);
		this.fileChannelFactory = Objects.requireNonNull(fileChannelFactory);
		this.writeStrategy = writeStrategy;
	}

	@Override
	public long append(ByteBuffer data) {
		try {
			FileChannel outputChannel = getOutputChannel();
			long result = entryReader.append(data, outputChannel);
			switch (writeStrategy) {
			case SAFE:
				outputChannel.force(true);
				break;
			case SUPERSAFE:
				closeOutputStream();
				break;
			case FAST: break;
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ByteBuffer readEntry(long position) {
		try {
			return entryReader.readEntry(position, getInputChannel());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long size() {
		if (size == null) {
			try {
				size = getInputChannel().size();
			} catch (Exception e) {
				size = 0L;
			}
		}
		return size;
	}

	private FileChannel getOutputChannel() {
		size = null;
		if (outputChannel != null) {
			return outputChannel;
		}
		closeInputStream();
		outputStream = fileChannelFactory.getOutputChannel(fileName);
		outputChannel = outputStream.getChannel();
		return outputChannel;
	}

	private FileChannel getInputChannel() {
		if (inputChannel != null) {
			return inputChannel;
		}
		closeOutputStream();
		inputStream = fileChannelFactory.getInputChannel(fileName);
		inputChannel = inputStream.getChannel();
		return inputChannel;
	}

	@Override
	public void close() {
		closeInputStream();
		closeOutputStream();
		fileChannelFactory = null;
	}

	private void closeInputStream() {
		if (inputChannel != null) {
			try { inputChannel.close(); } catch (IOException e) { System.out.println("Could not close file. Reason: " + e);/*TODO: Logging*/ }
			inputChannel = null;
		}
		if (inputStream != null) {
			try { inputStream.close(); } catch (IOException e) { System.out.println("Could not close file. Reason: " + e);/*TODO: Logging*/ }
			inputStream = null;
		}
	}

	private void closeOutputStream() {
		if (outputChannel != null) {
			try { outputChannel.close(); } catch (IOException e) { System.out.println("Could not close file. Reason: " + e);/*TODO: Logging*/ }
			outputChannel = null;
		}
		if (outputStream != null) {
			try {
				outputStream.flush();
			} catch (IOException e) { /*TODO: Logging*/ }
			try { outputStream.close(); } catch (IOException e) { System.out.println("Could not close file. Reason: " + e);/*TODO: Logging*/ }
			outputStream = null;
		}
	}

	@Override
	public String toString() {
		return "FlippingEntryFile [fileName=" + fileName + ", entryReader="
				+ entryReader + ", inputStream=" + inputStream
				+ ", inputChannel=" + inputChannel + ", outputStream="
				+ outputStream + ", outputChannel=" + outputChannel + ", size="
				+ size + ", fileChannelFactory=" + fileChannelFactory
				+ ", writeStrategy=" + writeStrategy + "]";
	}

}
