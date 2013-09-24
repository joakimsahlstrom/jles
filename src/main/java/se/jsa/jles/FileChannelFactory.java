package se.jsa.jles;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public interface FileChannelFactory {
	FileOutputStream getOutputChannel(String fileName);
	FileInputStream getInputChannel(String fileName);
}
