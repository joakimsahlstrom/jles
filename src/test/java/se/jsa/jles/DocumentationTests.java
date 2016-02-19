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

import java.util.Iterator;

import org.junit.Ignore;
import org.junit.Test;

public class DocumentationTests {

	public static class UserRegisteredEvent {
		private long id;
		private String userName;
		
		public UserRegisteredEvent() { // for jles
		}
		
		public UserRegisteredEvent(long id, String userName) {
			this.id = id;
			this.userName = userName;
		}
		
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
	}
	
	public static class ChangeUserNameEvent {
		private long id;
		private String userName;
		
		public ChangeUserNameEvent() { // for jles
		}
		
		public ChangeUserNameEvent(long id, String userName) {
			this.id = id;
			this.userName = userName;
		}
		
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
	}
	
	@Test
	public void writeReadEvent() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
		eventStore.write(new UserRegisteredEvent(1L, "test"));
		
		// Event reading is centered around java.lang.Iterable
		Iterable<Object> readEvents = eventStore.readEvents(EventQuery.select(UserRegisteredEvent.class)); 
		Iterator<Object> events = readEvents.iterator();
		UserRegisteredEvent event = (UserRegisteredEvent) events.next();
		assertEquals(1L, event.getId());
		assertEquals("test", event.getUserName());
		assertFalse(events.hasNext());
	}
	
	@Test
	public void writeMultipleEventTypes() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
		eventStore.write(new UserRegisteredEvent(1L, "test"));
		eventStore.write(new ChangeUserNameEvent(1L, "test2"));
		
		Iterator<Object> events = eventStore.readEvents(EventQuery
				.select(UserRegisteredEvent.class)
				.and(ChangeUserNameEvent.class))
				.iterator();
		assertEquals("test", ((UserRegisteredEvent) events.next()).getUserName());
		assertEquals("test2", ((ChangeUserNameEvent) events.next()).getUserName());
		assertFalse(events.hasNext());
	}
	
	@Test
	public void queryWithFieldComparison() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
		eventStore.write(new UserRegisteredEvent(1L, "test"));
		eventStore.write(new ChangeUserNameEvent(1L, "test2"));
		
		Iterator<Object> events = eventStore.readEvents(EventQuery
				// capital I in Id as everything in the method name after get... is matches as is
				.select(UserRegisteredEvent.class).where("Id").is(1L) 
				.and(ChangeUserNameEvent.class).where("Id").is(1L))
				.iterator();
		assertEquals("test", ((UserRegisteredEvent) events.next()).getUserName());
		assertEquals("test2", ((ChangeUserNameEvent) events.next()).getUserName());
		assertFalse(events.hasNext());
	}
	
	@Test
	@Ignore("Bug")
	public void complexFieldQuery() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
		eventStore.write(new UserRegisteredEvent(1L, "test1"));
		eventStore.write(new UserRegisteredEvent(2L, "test2"));
		eventStore.write(new UserRegisteredEvent(3L, "test3"));
		Iterator<Object> events = eventStore.readEvents(EventQuery
				.select(UserRegisteredEvent.class).where("Id").isGreaterThan(1L).where("Id").isLessThan(3L)) 
				.iterator();
		assertEquals("test2", ((UserRegisteredEvent) events.next()).getUserName());
		assertFalse(events.hasNext()); 
	}
	
	@Test
	public void iteratorsAreLive() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
		eventStore.write(new UserRegisteredEvent(1L, "test"));
		
		Iterator<Object> events = eventStore.readEvents(EventQuery
				.select(UserRegisteredEvent.class).where("Id").is(1L)
				.and(ChangeUserNameEvent.class).where("Id").is(1L))
				.iterator();
		assertEquals("test", ((UserRegisteredEvent) events.next()).getUserName());
		assertFalse(events.hasNext());

		eventStore.write(new ChangeUserNameEvent(1L, "test2"));
		assertTrue(events.hasNext());
		assertEquals("test2", ((ChangeUserNameEvent) events.next()).getUserName());
		assertFalse(events.hasNext());
	}
	
	@Test
	public void addFieldIndexing() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer()
				.addIndexing(UserRegisteredEvent.class, "Id")
				.addIndexing(ChangeUserNameEvent.class, "Id")
				.configure();
		
		// Iterating over this query will now execute faster
		Iterator<Object> events = eventStore.readEvents(EventQuery
				.select(UserRegisteredEvent.class).where("Id").is(1L)
				.and(ChangeUserNameEvent.class).where("Id").is(1L))
				.iterator();
	}
	
	@Test
	public void addInMemoryIndexing() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer()
				.addInMemoryIndexing(UserRegisteredEvent.class, "Id")
				.addInMemoryIndexing(ChangeUserNameEvent.class, "Id")
				.configure();
		
		// Iterating over this query will now execute faster
		Iterator<Object> events = eventStore.readEvents(EventQuery
				.select(UserRegisteredEvent.class).where("Id").is(1L)
				.and(ChangeUserNameEvent.class).where("Id").is(1L))
				.iterator();
	}
	
}
