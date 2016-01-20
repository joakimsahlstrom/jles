package se.jsa.jles.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import se.jsa.jles.EventStoreConfigurer.WriteStrategy;
import se.jsa.jles.FileChannelFactory;
import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.InMemoryFileRepository;
import se.jsa.jles.internal.file.ThreadSafeEntryFile;

public class EntryFileFactory {

	private final FileChannelFactory fileChannelFactory;
	private final InMemoryFileRepository inMemoryFileRepository;
	private final AtomicReference<Boolean> multiThreadedEnvironment;

	private final List<String> files = new ArrayList<String>();
	private WriteStrategy writeStrategy = WriteStrategy.FAST;

	public EntryFileFactory(FileChannelFactory fileChannelFactory,
			InMemoryFileRepository inMemoryFileRepository,
			AtomicReference<Boolean> multiThreadedEnvironment) {
		this.fileChannelFactory = fileChannelFactory;
		this.inMemoryFileRepository = inMemoryFileRepository;
		this.multiThreadedEnvironment = multiThreadedEnvironment;
	}

	public void setWriteStrategy(WriteStrategy writeStrategy2) {
		this.writeStrategy = writeStrategy2;
	}

	public EntryFile createEntryFile(String fileName) {
		if (fileChannelFactory == null) {
			return inMemoryFileRepository.getEntryFile(fileName);
		}

		EntryFile flippingEntryFile = new FlippingEntryFile(fileName, fileChannelFactory, writeStrategy);
		if (multiThreadedEnvironment.get()) {
			flippingEntryFile = new ThreadSafeEntryFile(flippingEntryFile);
		}
		files.add(fileName);
		return flippingEntryFile;
	}

	public List<String> getFiles() {
		return files;
	}

}
