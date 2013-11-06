package se.jsa.jles.internal.file;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.jsa.jles.internal.EntryFile;

public abstract class EntryFileContract {

	private EntryFile entryFile;

	public abstract EntryFile createEntryFile();
	public abstract void closeEntryFile(EntryFile entryFile);

	@Before
	public void setup() {
		this.entryFile = createEntryFile();
	}

	@After
	public void teardown() {
		closeEntryFile(this.entryFile);
	}

	@Test
	public void size_InitialIsZero() throws Exception {
		assertEquals(0L, entryFile.size());
	}

	@Test
	public void size_SizeIncreasedWhenAddingBufferData() throws Exception {
		entryFile.append(generateEntry(1L, 2));
		assertEquals(14, entryFile.size());
	}

	@Test
	public void size_EventSizesAppendsToTotalSize() throws Exception {
		entryFile.append(generateEntry(1L, 2));
		entryFile.append(generateEntry(2L, 10));
		entryFile.append(generateEntry(3L, 5));
		assertEquals(14 + 22 + 17, entryFile.size());
	}

	@Test
	public void readEntryReturnsCorrectEntry() throws Exception {
		ByteBuffer testEntry = generateEntry(1L, 20);
		long position = entryFile.append(testEntry);
		Assert.assertArrayEquals(testEntry.array(), entryFile.readEntry(position).array());
	}

	@Test
	public void multipleEntriesCanBeWrittenAndRead() throws Exception {
		ByteBuffer testEntry1 = generateEntry(1L, 20);
		ByteBuffer testEntry2 = generateEntry(2L, 200);
		ByteBuffer testEntry3 = generateEntry(3L, 10);
		long position1 = entryFile.append(testEntry1);
		long position2 = entryFile.append(testEntry2);
		long position3 = entryFile.append(testEntry3);
		Assert.assertArrayEquals(testEntry1.array(), entryFile.readEntry(position1).array());
		Assert.assertArrayEquals(testEntry2.array(), entryFile.readEntry(position2).array());
		Assert.assertArrayEquals(testEntry3.array(), entryFile.readEntry(position3).array());
	}

	@Test
	public void multipleEntriesCanBeWrittenAndReadInterleaved() throws Exception {
		ByteBuffer testEntry1 = generateEntry(1L, 20);
		ByteBuffer testEntry2 = generateEntry(2L, 200);
		ByteBuffer testEntry3 = generateEntry(3L, 10);
		long position1 = entryFile.append(testEntry1);
		Assert.assertArrayEquals(testEntry1.array(), entryFile.readEntry(position1).array());
		long position2 = entryFile.append(testEntry2);
		Assert.assertArrayEquals(testEntry1.array(), entryFile.readEntry(position1).array());
		Assert.assertArrayEquals(testEntry2.array(), entryFile.readEntry(position2).array());
		long position3 = entryFile.append(testEntry3);
		Assert.assertArrayEquals(testEntry1.array(), entryFile.readEntry(position1).array());
		Assert.assertArrayEquals(testEntry2.array(), entryFile.readEntry(position2).array());
		Assert.assertArrayEquals(testEntry3.array(), entryFile.readEntry(position3).array());
		Assert.assertArrayEquals(testEntry1.array(), entryFile.readEntry(position1).array());
	}

	private ByteBuffer generateEntry(long id, int size) {
		ByteBuffer result = ByteBuffer.allocate(8 + 4 + size);
		result.putLong(id);
		result.putInt(size);
		for (int i = 0; i < size; i++) {
			result.put((byte)(-128 + Math.random() * 256));
		}
		result.rewind();
		return result;
	}

}
