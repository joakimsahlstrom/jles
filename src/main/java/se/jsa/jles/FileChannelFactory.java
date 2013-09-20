package se.jsa.jles;

import java.nio.channels.FileChannel;

public interface FileChannelFactory {
	FileChannel getOutputChannel(String fileName);
	FileChannel getInputChannel(String fileName);
}
