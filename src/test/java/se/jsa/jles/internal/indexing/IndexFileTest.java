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
package se.jsa.jles.internal.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.EventFileTest;
import se.jsa.jles.internal.EventId;
import se.jsa.jles.internal.fields.LongField;
import se.jsa.jles.internal.fields.StorableLongField;
import se.jsa.jles.internal.fields.StringField;
import se.jsa.jles.internal.file.SynchronousEntryFile;
import se.jsa.jles.internal.indexing.events.EventIndex;

public class IndexFileTest {

	@After
	public void setup() {
		delete("test.if");
	}

	private boolean delete(String fileName) {
		File file = new File(fileName);
		int count = 0;
		while (file.exists() && !file.delete() && count++ < 10) {
			System.out.println("Failed to delete file: " + fileName);
		}
		return true;
	}

	@Test
	public void longEventIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new LongField(EventFileTest.SingleLongEvent.class, "Val"), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, new EventFileTest.SingleLongEvent(23L));
		Iterable<EventId> indexes = indexFile.readIndicies(EventIndex.ALWAYS_MATCHER);
		Iterator<EventId> iterator = indexes.iterator();
		iterator.next();
		assertFalse(iterator.hasNext());

		indexFile.close();
	}

	@Test
	public void severalLongEventIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new LongField(EventFileTest.SingleLongEvent.class, "Val"), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, new EventFileTest.SingleLongEvent(23L));
		indexFile.writeIndex(3, new EventFileTest.SingleLongEvent(100239923L));
		Iterable<EventId> indexes = indexFile.readIndicies(EventIndex.ALWAYS_MATCHER);
		Iterator<EventId> iterator = indexes.iterator();

		EventId ie1 = iterator.next();
		EventId ie2 = iterator.next();
		assertFalse(iterator.hasNext());
		assertEquals(0L, ie1.toLong());
		assertEquals(3L, ie2.toLong());

		indexFile.close();
	}

	@Test
	public void severalStringIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new StringField(EventFileTest.SingleStringEvent.class, "Val"), new SynchronousEntryFile("test.if"));
		EventFileTest.SingleStringEvent event = new EventFileTest.SingleStringEvent("apa");
		indexFile.writeIndex(0, event);
		indexFile.writeIndex(3, new EventFileTest.SingleStringEvent("bapa"));
		indexFile.writeIndex(27, new EventFileTest.SingleStringEvent("kanin�ra"));
		Iterable<EventId> indexes = indexFile.readIndicies(EventIndex.ALWAYS_MATCHER);
		Iterator<EventId> iterator = indexes.iterator();

		EventId ie1 = iterator.next();
		EventId ie2 = iterator.next();
		EventId ie3 = iterator.next();
		assertFalse(iterator.hasNext());
		assertEquals(0L, ie1.toLong());
		assertEquals(3L, ie2.toLong());
		assertEquals(27L, ie3.toLong());

		indexFile.close();
	}

	@Test
	public void longIndexCanBeWrittenAndReadBack() throws Exception {
		IndexFile indexFile = new IndexFile(new StorableLongField(), new SynchronousEntryFile("test.if"));
		indexFile.writeIndex(0, 12L);
		indexFile.writeIndex(1, 1201L);
		indexFile.writeIndex(2, 12L);
		indexFile.writeIndex(3, 13L);
		Iterable<EventId> indexes = indexFile.readIndicies(EventIndex.ALWAYS_MATCHER);
		Iterator<EventId> iterator = indexes.iterator();

		EventId ie1 = iterator.next();
		EventId ie2 = iterator.next();
		EventId ie3 = iterator.next();
		EventId ie4 = iterator.next();
		assertFalse(iterator.hasNext());
		assertEquals(0, ie1.toLong());
		assertEquals(1, ie2.toLong());
		assertEquals(2, ie3.toLong());
		assertEquals(3, ie4.toLong());

		indexFile.close();
	}

	@Test
	public void hasNext() throws Exception {
		IndexFile indexFile = new IndexFile(new StorableLongField(), new SynchronousEntryFile("test.if"));

		Iterator<EventId> iterator = indexFile.readIndicies(EventIndex.ALWAYS_MATCHER).iterator();
		assertFalse(iterator.hasNext());
		assertFalse(iterator.hasNext());

		indexFile.close();
	}

}
