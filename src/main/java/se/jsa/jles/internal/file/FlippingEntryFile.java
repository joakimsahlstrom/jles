package se.jsa.jles.internal.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
			try { inputChannel.close(); } catch (IOException e) { }
			inputChannel = null;
		}
		if (inputStream != null) {
			try { inputStream.close(); } catch (IOException e) { }
			inputStream = null;
		}
	}

	private void closeOutputStream() {
		if (outputChannel != null) {
			try { outputChannel.close(); } catch (IOException e) { }
			outputChannel = null;
		}
		if (outputStream != null) {
			try {
				outputStream.flush();
			} catch (IOException e) { }
			try { outputStream.close(); } catch (IOException e) { }
			outputStream = null;
		}
	}

}
