package se.jsa.jles.internal.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import se.jsa.jles.FileChannelFactory;

public class StreamBasedChannelFactory implements FileChannelFactory {

	FileOutputStream fileOutputStream = null;
	FileInputStream fileInputStream = null;

	@Override
	public FileOutputStream getOutputChannel(String fileName) {
		try {
			fileOutputStream = new FileOutputStream(fileName, true);
			fileInputStream = null;
			return fileOutputStream;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FileInputStream getInputChannel(String fileName) {
		try {
			fileInputStream = new FileInputStream(fileName);
			fileOutputStream = null;
			return fileInputStream;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		if (fileOutputStream != null) {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
