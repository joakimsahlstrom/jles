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

import java.util.Iterator;

import org.junit.Test;

public class DocumentationTests {

	public static class UserRegisteredEvent {
		long id;
		String userName;
		
		public UserRegisteredEvent() {
			// for jles
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
	
	@Test
	public void writeReadEvent() throws Exception {
		EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
		eventStore.write(new UserRegisteredEvent(1L, "test"));
		
		Iterable<Object> readEvents = eventStore.readEvents(EventQuery.select(UserRegisteredEvent.class));
		Iterator<Object> events = readEvents.iterator();
		UserRegisteredEvent event = (UserRegisteredEvent) events.next();
		assertEquals(1L, event.getId());
		assertEquals("test", event.getUserName());
		assertFalse(events.hasNext());
	}
	
}
