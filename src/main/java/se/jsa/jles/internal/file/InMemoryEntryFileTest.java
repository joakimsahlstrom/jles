package se.jsa.jles.internal.file;

import se.jsa.jles.internal.EntryFile;

public class InMemoryEntryFileTest extends EntryFileContract {

	@Override
	public EntryFile createEntryFile() {
		return new InMemoryEntryFile();
	}

	@Override
	public void closeEntryFile(EntryFile entryFile) {
		// do nothing
	}

}
