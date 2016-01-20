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
package se.jsa.jles.internal.fields;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class EventFieldFactoryTest {

	public static class EmptyEvent {
		// empty by design
	}

	public static class BadEvent {
		Class<?> badField;
		public BadEvent() {
		}
		public Class<?> getBadField() {
			return badField;
		}
		public void setBadField(Class<?> badField) {
			this.badField = badField;
		}
	}

	public static class SingleIntegerEvent {
		public int val;

		public SingleIntegerEvent() {
		}

		public SingleIntegerEvent(int val) {
			this.val = val;
		}

		public int getVal() {
			return val;
		}

		public void setVal(int val) {
			this.val = val;
		}
	}

	public static class SingleStringEvent {
		public String val;

		public SingleStringEvent() {
		}

		public SingleStringEvent(String val) {
			this.val = val;
		}

		public String getVal() {
			return val;
		}

		public void setVal(String val) {
			this.val = val;
		}
	}

	public static class IntegerStringEvent {
		public int i;
		public String s;

		public IntegerStringEvent() {
		}

		public IntegerStringEvent(int i, String s) {
			this.i = i;
			this.s = s;
		}

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}

		public String getS() {
			return s;
		}

		public void setS(String s) {
			this.s = s;
		}
	}

	@Test(expected = Exception.class)
	public void throwsExceptionWhenBadFieldFound() throws Exception {
		new EventFieldFactory().fromEventType(BadEvent.class);
	}

	@Test
	public void canBuildEventFieldsFromEmptyEventType() throws Exception {
		assertEquals(0, new EventFieldFactory().fromEventType(EmptyEvent.class).size());
	}

	@Test
	public void canBuildEventFieldsFromSingleIntegerEventType() throws Exception {
		List<EventField> eventFields = new EventFieldFactory().fromEventType(SingleIntegerEvent.class);
		assertEquals(
				asSet(new IntegerField(SingleIntegerEvent.class, "Val")),
				asSet(eventFields));
	}

	@Test
	public void canBuildEventFieldsFromSingleStringEventType() throws Exception {
		List<EventField> eventFields = new EventFieldFactory().fromEventType(SingleStringEvent.class);
		assertEquals(
				asSet(new IntegerField(SingleStringEvent.class, "Val")),
				asSet(eventFields));
	}

	@Test
	public void canBuildEventFieldsFromIntegerStringEventType() throws Exception {
		List<EventField> eventFields = new EventFieldFactory().fromEventType(IntegerStringEvent.class);
		assertEquals(
				asSet(new IntegerField(IntegerStringEvent.class, "I"), new StringField(IntegerStringEvent.class, "S")),
				asSet(eventFields));
	}

	private Set<EventField> asSet(List<EventField> eventFields) {
		return new HashSet<EventField>(eventFields);
	}

	private Set<EventField> asSet(EventField... fields) {
		return new HashSet<EventField>(Arrays.asList(fields));
	}

}
