package se.jsa.jles.internal.file;

import java.io.File;

import se.jsa.jles.internal.EntryFile;



public class FlippingEntryFileTest extends EntryFileContract {

	StreamBasedChannelFactory fileChannelFactory = new StreamBasedChannelFactory();

	@Override
	public EntryFile createEntryFile() {
		deleteFile();
		return new FlippingEntryFile("testEntryFile.ef", fileChannelFactory);
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
