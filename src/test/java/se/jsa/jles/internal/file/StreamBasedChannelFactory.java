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
	
	@Override
	public boolean fileExits(String fileName) {
		return new File(fileName).exists();
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
