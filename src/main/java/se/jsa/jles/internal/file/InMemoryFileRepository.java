package se.jsa.jles.internal.file;

import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.EventStoreConfigurer;
import se.jsa.jles.internal.EntryFile;

/**
 * Used for test setups (in {@link EventStoreConfigurer}) when we need to fake file persistence between event store instantiations
 */
public class InMemoryFileRepository {
	private final Map<String, EntryFile> files = new HashMap<String, EntryFile>();

	public EntryFile getEntryFile(String fileName) {
		if (!files.containsKey(fileName)) {
			files.put(fileName, new InMemoryEntryFile());
		}
		return files.get(fileName);
	}
}
