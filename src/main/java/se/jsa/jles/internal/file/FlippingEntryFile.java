package se.jsa.jles.internal.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import se.jsa.jles.FileChannelFactory;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.util.Objects;

public class FlippingEntryFile implements EntryFile {

	private final String fileName;
	private final EntryFileSerializer entryReader = new EntryFileSerializer();

	private FileChannel inputChannel = null;
	private FileChannel outputChannel = null;
	private Long size = null;
	private final FileChannelFactory fileChannelFactory;
	private final boolean safeWrite;

	public FlippingEntryFile(String fileName, FileChannelFactory fileChannelFactory) {
		this(fileName, fileChannelFactory, false);
	}

	public FlippingEntryFile(String fileName, FileChannelFactory fileChannelFactory, boolean safeWrite) {
		this.fileName = Objects.requireNonNull(fileName);
		this.fileChannelFactory = Objects.requireNonNull(fileChannelFactory);
		this.safeWrite = safeWrite;
	}

	@Override
	public long append(ByteBuffer data) {
		try {
			long result = entryReader.append(data, getOutputChannel());
			if (safeWrite) {
				close();
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
			FileChannel channel = null;
			try {
				channel = fileChannelFactory.getInputChannel(fileName);
				size = channel.size();
			} catch (Exception e) {
				size = 0L;
			} finally {
				if (channel != null) {
					try {
						channel.close();
					} catch (IOException e) {
						// do nothing;
					}
				}
			}
		}
		return size;
	}

	@Override
	public void close() {
		if (inputChannel != null) {
			try { inputChannel.close(); } catch (IOException e) { /**/ }
			inputChannel = null;
		}
		if (outputChannel != null) {
			try { outputChannel.close(); } catch (IOException e) { /**/ }
			outputChannel = null;
		}
	}

	private FileChannel getOutputChannel() throws IOException {
		size = null;
		if (outputChannel != null) {
			return outputChannel;
		}
		if (inputChannel != null) {
			inputChannel.close();
			inputChannel = null;
		}
		outputChannel = fileChannelFactory.getOutputChannel(fileName);
		return outputChannel;
	}

	private FileChannel getInputChannel() throws IOException {
		if (inputChannel != null) {
			return inputChannel;
		}
		if (outputChannel != null) {
			outputChannel.close();
			outputChannel = null;
		}
		inputChannel = fileChannelFactory.getInputChannel(fileName);
		return inputChannel;
	}

}
