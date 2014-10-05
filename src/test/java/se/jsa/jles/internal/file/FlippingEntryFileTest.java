package se.jsa.jles.internal.file;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.jsa.jles.FileChannelFactory;
import se.jsa.jles.internal.EntryFile;


@RunWith(value = Parameterized.class)
public class FlippingEntryFileTest extends EntryFileContract {

	private interface EntryFileFactory {
		public EntryFile create(String fileName, FileChannelFactory fileChannelFactory);
	}
	
	@Parameters
	public static Collection<Object[]> entryFiles() {
		return Arrays.asList(new Object[] {
				new EntryFileFactory() {
					@Override
					public EntryFile create(String fileName, FileChannelFactory fileChannelFactory) {
						return new FlippingEntryFile(fileName, fileChannelFactory, false);
					}
					@Override
					public String toString() { return "FlippingEntryFile, no safe writes"; }
				}
			},
			new Object[] {
				new EntryFileFactory() {
					@Override
					public EntryFile create(String fileName, FileChannelFactory fileChannelFactory) {
						return new FlippingEntryFile(fileName, fileChannelFactory, true);
					}
					@Override
					public String toString() { return "FlippingEntryFile, safe writes"; }
				}
			});
	}
	
	private StreamBasedChannelFactory fileChannelFactory = new StreamBasedChannelFactory();
	private EntryFileFactory entryFileFactory;
	
	public FlippingEntryFileTest(EntryFileFactory entryFileFactory) {
		this.entryFileFactory = entryFileFactory;
	}
	
	@Override
	public EntryFile createEntryFile() {
		deleteFile();
		return entryFileFactory.create("testEntryFile.ef", fileChannelFactory);
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
