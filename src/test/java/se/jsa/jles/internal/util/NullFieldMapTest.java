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
package se.jsa.jles.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.jsa.jles.internal.fields.BooleanField;
import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.LongField;
import se.jsa.jles.internal.fields.StringField;
import se.jsa.jles.internal.testevents.ObjectTestEvent;
import se.jsa.jles.internal.testevents.TestEvent;



public class NullFieldMapTest {

	@Test
	public void canCalculateNullFieldSize() throws Exception {
		assertEquals(0, NullFieldMap.getSizeInBytes(Arrays.<EventField>asList()));
		assertEquals(0, NullFieldMap.getSizeInBytes(Arrays.<EventField>asList(new BooleanField(TestEvent.class, "First"))));
		assertEquals(1, NullFieldMap.getSizeInBytes(Arrays.<EventField>asList(new BooleanField(ObjectTestEvent.class, "First"))));
		assertEquals(1, NullFieldMap.getSizeInBytes(Arrays.<EventField>asList(new BooleanField(ObjectTestEvent.class, "First"), new BooleanField(TestEvent.class, "First"))));
		assertEquals(1, NullFieldMap.getSizeInBytes(createEventFieldList(8)));
		assertEquals(2, NullFieldMap.getSizeInBytes(createEventFieldList(9)));
		assertEquals(2, NullFieldMap.getSizeInBytes(createEventFieldList(16)));
		assertEquals(3, NullFieldMap.getSizeInBytes(createEventFieldList(17)));
	}

	@Test
	public void canCreateNullMapFromEvent() throws Exception {
		List<EventField> objectTestEventFields = buildObjectTestEventFields();
		NullFieldMap nullFieldMap = NullFieldMap.buildFromEvent(objectTestEventFields, new ObjectTestEvent("name", 1L, Boolean.FALSE));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(0)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(1)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(2)));

		nullFieldMap = NullFieldMap.buildFromEvent(objectTestEventFields, new ObjectTestEvent(null, 1L, Boolean.FALSE));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(0)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(1)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(2)));

		nullFieldMap = NullFieldMap.buildFromEvent(objectTestEventFields, new ObjectTestEvent("name", null, Boolean.FALSE));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(0)));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(1)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(2)));

		nullFieldMap = NullFieldMap.buildFromEvent(objectTestEventFields, new ObjectTestEvent("name", 1L, null));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(0)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(1)));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(2)));

		nullFieldMap = NullFieldMap.buildFromEvent(objectTestEventFields, new ObjectTestEvent(null, null, null));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(0)));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(1)));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(2)));
	}

	@Test
	public void canRestoreNullFieldFromByteArray() throws Exception {
		List<EventField> objectTestEventFields = buildObjectTestEventFields();
		NullFieldMap nullFieldMap = NullFieldMap.buildFromEvent(objectTestEventFields, new ObjectTestEvent("name", 1L, null));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(0)));
		assertFalse(nullFieldMap.isFieldNull(objectTestEventFields.get(1)));
		assertTrue(nullFieldMap.isFieldNull(objectTestEventFields.get(2)));

		NullFieldMap restoredMap = NullFieldMap.buildFromBuffer(objectTestEventFields, ByteBuffer.wrap(nullFieldMap.getMask()));
		assertFalse(restoredMap.isFieldNull(objectTestEventFields.get(0)));
		assertFalse(restoredMap.isFieldNull(objectTestEventFields.get(1)));
		assertTrue(restoredMap.isFieldNull(objectTestEventFields.get(2)));
	}

	@Test
	public void alwaysRespondsFalseOnPrimitiveFieldNullTest() throws Exception {
		List<EventField> testEventFields = buildTestEventFields();
		NullFieldMap nullFieldMap = NullFieldMap.buildFromEvent(testEventFields, new TestEvent("name", 1L, Boolean.FALSE));
		assertFalse(nullFieldMap.isFieldNull(testEventFields.get(0)));
		assertFalse(nullFieldMap.isFieldNull(testEventFields.get(1)));
		assertFalse(nullFieldMap.isFieldNull(testEventFields.get(2)));
	}

	private List<EventField> buildObjectTestEventFields() {
		return Arrays.<EventField>asList(new StringField(ObjectTestEvent.class, "Name"), new LongField(ObjectTestEvent.class, "Id"), new BooleanField(ObjectTestEvent.class, "First"));
	}

	private List<EventField> buildTestEventFields() {
		return Arrays.<EventField>asList(new StringField(TestEvent.class, "Name"), new LongField(TestEvent.class, "Id"), new BooleanField(TestEvent.class, "First"));
	}

	private List<EventField> createEventFieldList(int length) {
		ArrayList<EventField> result = new ArrayList<EventField>();
		for (int i = 0; i < length; i++) {
			result.add(new BooleanField(ObjectTestEvent.class, "First"));
		}
		return result;
	}

}
