package se.jsa.jles.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.jsa.jles.internal.IndexFile;
import se.jsa.jles.internal.IndexFile.IndexEntry;
import se.jsa.jles.internal.fields.LongField;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.fields.StringField;
import se.jsa.jles.internal.file.SynchronousEntryFile;

public class IndexFileTest {

	@Before
	@After
	public void setup() {
		new File("test.if").delete();
	}

	@Test
	public void longEventIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new LongField(EventFileTest.SingleLongEvent.class, "Val"), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, new EventFileTest.SingleLongEvent(23L));
		Iterable<IndexEntry<Long>> indexes = indexFile.readIndicies(Long.class);
		Iterator<IndexEntry<Long>> iterator = indexes.iterator();
		assertEquals(Long.valueOf(23), iterator.next().getKey());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void severalLongEventIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new LongField(EventFileTest.SingleLongEvent.class, "Val"), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, new EventFileTest.SingleLongEvent(23L));
		indexFile.writeIndex(3, new EventFileTest.SingleLongEvent(100239923L));
		Iterable<IndexEntry<Long>> indexes = indexFile.readIndicies(Long.class);
		Iterator<IndexEntry<Long>> iterator = indexes.iterator();

		IndexEntry<Long> ie1 = iterator.next();
		IndexEntry<Long> ie2 = iterator.next();
		assertFalse(iterator.hasNext());
		assertEquals(0L, ie1.getEventIndex());
		assertEquals(3L, ie2.getEventIndex());
		assertEquals(Long.valueOf(23), ie1.getKey());
		assertEquals(Long.valueOf(100239923L), ie2.getKey());
	}

	@Test
	public void severalStringIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new StringField(EventFileTest.SingleStringEvent.class, "Val"), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, new EventFileTest.SingleStringEvent("apa"));
		indexFile.writeIndex(3, new EventFileTest.SingleStringEvent("bapa"));
		indexFile.writeIndex(27, new EventFileTest.SingleStringEvent("kanin�ra"));
		Iterable<IndexEntry<String>> indexes = indexFile.readIndicies(String.class);
		Iterator<IndexEntry<String>> iterator = indexes.iterator();

		IndexEntry<String> ie1 = iterator.next();
		IndexEntry<String> ie2 = iterator.next();
		IndexEntry<String> ie3 = iterator.next();
		assertFalse(iterator.hasNext());
		assertEquals(0L, ie1.getEventIndex());
		assertEquals(3L, ie2.getEventIndex());
		assertEquals(27L, ie3.getEventIndex());
		assertEquals("apa", ie1.getKey());
		assertEquals("bapa", ie2.getKey());
		assertEquals("kanin�ra", ie3.getKey());
	}

	@Test
	public void longIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new StorableLongField(), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, 12L);
		indexFile.writeIndex(1, 1201L);
		indexFile.writeIndex(2, 12L);
		indexFile.writeIndex(3, 13L);
		Iterable<IndexEntry<Long>> indexes = indexFile.readIndicies(Long.class);
		Iterator<IndexEntry<Long>> iterator = indexes.iterator();

		IndexEntry<Long> ie1 = iterator.next();
		IndexEntry<Long> ie2 = iterator.next();
		IndexEntry<Long> ie3 = iterator.next();
		IndexEntry<Long> ie4 = iterator.next();
		assertFalse(iterator.hasNext());
		assertEquals(0, ie1.getEventIndex());
		assertEquals(1, ie2.getEventIndex());
		assertEquals(2, ie3.getEventIndex());
		assertEquals(3, ie4.getEventIndex());
		assertEquals(Long.valueOf(12), ie1.getKey());
		assertEquals(Long.valueOf(1201), ie2.getKey());
		assertEquals(Long.valueOf(12), ie3.getKey());
		assertEquals(Long.valueOf(13), ie4.getKey());
	}

}
