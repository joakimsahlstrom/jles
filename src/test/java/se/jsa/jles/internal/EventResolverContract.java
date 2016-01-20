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
package se.jsa.jles.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import se.jsa.jles.internal.eventdefinitions.EventResolver;
import se.jsa.jles.internal.testevents.TestEvent;

public abstract class EventResolverContract {

	protected abstract EventResolver getEventResolver();

	@Test
	public void ifAnEventIsAlreadySerializableItIsReturnedAsIsFromAllMethods() throws Exception {
		TestEvent testEvent = new TestEvent("n", 1L, true);
		assertEquals(testEvent, getEventResolver().getSerializableEvent(testEvent));
		assertEquals(TestEvent.class, getEventResolver().getSerializableEventTypes(new Class[] {TestEvent.class})[0]);
	}

}
