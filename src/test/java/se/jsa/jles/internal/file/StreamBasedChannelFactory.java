package se.jsa.jles.internal.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import se.jsa.jles.FileChannelFactory;

public class StreamBasedChannelFactory implements FileChannelFactory {

	@Override
	public FileChannel getOutputChannel(String fileName) {
		try {
			return new FileOutputStream(fileName, true).getChannel();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FileChannel getInputChannel(String fileName) {
		try {
			return new FileInputStream(fileName).getChannel();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
