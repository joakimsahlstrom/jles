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
package se.jsa.jles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.EventStoreTest.EmptyEvent;
import se.jsa.jles.EventStoreTest.EmptyEvent2;
import se.jsa.jles.EventStoreTest.EmptyEvent3;
import se.jsa.jles.EventStoreTest.TestEvent;
import se.jsa.jles.configuration.EventIndexingConfigurationMultiFile;
import se.jsa.jles.configuration.EventIndexingConfigurationSingleFile;
import se.jsa.jles.internal.testevents.MyEvent;
import se.jsa.jles.internal.testevents.ObjectTestEvent;

public class IndexingTest {

	private final EventStoreConfigurer configurer = EventStoreConfigurer
			.createMemoryOnlyConfigurer()
			.eventIndexing(EventIndexingConfigurationSingleFile.create().addIndexing(EmptyEvent3.class))
			.addInMemoryIndexing(TestEvent.class, "Name");

	private EventStore es = configurer.configure();

	@After
	public void after() {
		es.stop();
	}

	@Test
	public void canUseFieldIndex() throws Exception {
		TestEvent e1 = new TestEvent("a", 0, true);
		TestEvent e2 = new TestEvent("b", 1, true);
		TestEvent e3 = new TestEvent("a", 2, true);
		TestEvent e4 = new TestEvent("a", 3, true);
		es.write(e1);
		es.write(e2);
		es.write(e3);
		Iterator<Object> it = es.readEvents(EventQuery.select(TestEvent.class).where("Name").is("a")).iterator();
		assertEquals(e1, it.next());
		assertEquals(e3, it.next());
		assertFalse(it.hasNext());

		es.write(e4);
		assertTrue(it.hasNext());
		assertEquals(e4, it.next());
	}

	@Test
	public void canSupplyConstraint() throws Exception {
		es.write(new TestEvent("a", 0, true));
		es.write(new TestEvent("a", 1, true));
		es.write(new TestEvent("a", 2, true));
		List<Object> events = TestUtil.collect(es.readEvents(EventQuery.select(TestEvent.class).where("Id").is(1L)));
		assertEquals(Arrays.asList(new TestEvent("a", 1, true)), events);
	}

	@Test
	public void indexCanBeAddedAtLaterStartup() throws Exception {
		es.write(new TestEvent("a", 0, true));
		es.write(new MyEvent(1));
		es.write(new TestEvent("a", 1, true));
		es.write(new MyEvent(2));
		es.write(new TestEvent("a", 2, true));
		es.stop();

		es = configurer
				.eventIndexing(EventIndexingConfigurationSingleFile.create()
						.addIndexing(EmptyEvent3.class)
						.addIndexing(TestEvent.class)
						.addIndexing(MyEvent.class)
						)
				.configure();
		Thread.sleep(1);
		
		Iterator<Object> events = es.readEvents(EventQuery.select(TestEvent.class)).iterator();
		assertEquals(0L, ((TestEvent)events.next()).getId());
		assertEquals(1L, ((TestEvent)events.next()).getId());
		assertEquals(2L, ((TestEvent)events.next()).getId());
		assertFalse(events.hasNext());

		events = es.readEvents(EventQuery.select(MyEvent.class)).iterator();
		assertEquals(1, ((MyEvent)events.next()).getNum());
		assertEquals(2, ((MyEvent)events.next()).getNum());
		assertFalse(events.hasNext());
	}

	@Test
	public void fieldIndexCanBeAddedAtLaterStartup() throws Exception {
		es.write(new TestEvent("a", 0, true));
		es.write(new TestEvent("a", 1, true));
		es.write(new TestEvent("a", 2, true));
		es.stop();
		Thread.sleep(100);

		es = configurer
				.addIndexing(TestEvent.class, "Id")
				.configure();
		Thread.sleep(100);
		
		Iterator<Object> events = es.readEvents(EventQuery.select(TestEvent.class).where("Id").is(1L)).iterator();
		assertEquals(1L, ((TestEvent)events.next()).getId());
		assertFalse(events.hasNext());
	}

	@Test
	public void canIndexNullValues() throws Exception {
		es.stop();
		es = configurer
				.addIndexing(ObjectTestEvent.class, "First")
				.configure();

		es.write(new ObjectTestEvent("a", 0L, null));
		es.write(new ObjectTestEvent("a", 1L, true));
		es.write(new ObjectTestEvent("a", 2L, null));

		Iterator<Object> events = es.readEvents(EventQuery.select(ObjectTestEvent.class).where("First").is(null)).iterator();
		assertEquals(Long.valueOf(0L), ((ObjectTestEvent)events.next()).getId());
		assertEquals(Long.valueOf(2L), ((ObjectTestEvent)events.next()).getId());
		assertFalse(events.hasNext());
	}

	@Test
	public void indexPerformanceTest() throws Exception {
		List<Object> events = createEEvents(20000, 0.01d);

		for (Object event : events) {
			es.write(event);
		}

		long start2 = System.nanoTime();
		TestUtil.collect(es.readEvents(EventQuery.select(EmptyEvent2.class)));
		long end2 = System.nanoTime();

		long start3 = System.nanoTime();
		TestUtil.collect(es.readEvents(EventQuery.select(EmptyEvent3.class)));
		long end3 = System.nanoTime();

		long unindexedRead = end2 - start2;
		long indexedRead = end3 - start3;
		assertTrue("Indexed read should be at least a factor 2 faster under conditions given in this test case (" + indexedRead + " vs " + unindexedRead + ")", indexedRead * 2 < unindexedRead);
	}
	
	@Test
	public void canMigrateToMultiIndexFile() throws Exception {
		es.write(new TestEvent("a", 0, true));
		es.write(new MyEvent(1));
		es.write(new TestEvent("a", 1, true));
		es.write(new MyEvent(2));
		es.write(new TestEvent("a", 2, true));
		es.stop();

		es = configurer
				.eventIndexing(EventIndexingConfigurationMultiFile.create())
				.configure();
		
		Iterator<Object> events = es.readEvents(EventQuery.select(TestEvent.class)).iterator();
		assertEquals(0L, ((TestEvent)events.next()).getId());
		assertEquals(1L, ((TestEvent)events.next()).getId());
		assertEquals(2L, ((TestEvent)events.next()).getId());
		assertFalse(events.hasNext());

		events = es.readEvents(EventQuery.select(MyEvent.class)).iterator();
		assertEquals(1, ((MyEvent)events.next()).getNum());
		assertEquals(2, ((MyEvent)events.next()).getNum());
		assertFalse(events.hasNext());
		System.out.println(es.report());
	}

	private static Random random = new Random(System.nanoTime());
	public static List<Object> createEEvents(int num, double d) {
		List<Object> result = new ArrayList<Object>(num);
		for (int i = 0; i < num; i++) {
			if (random.nextDouble() < d) {
				result.add(new EmptyEvent2());
				result.add(new EmptyEvent3());
			} else {
				result.add(new EmptyEvent());
			}
		}
		return result;
	}

}
