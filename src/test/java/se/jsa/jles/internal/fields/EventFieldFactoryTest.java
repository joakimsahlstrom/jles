package se.jsa.jles.internal.fields;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import se.jsa.jles.internal.fields.EventField;
import se.jsa.jles.internal.fields.EventFieldFactory;
import se.jsa.jles.internal.fields.IntegerField;
import se.jsa.jles.internal.fields.StringField;

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
				asSet(new IntegerField(SingleIntegerEvent.class.getMethod("getVal"), SingleIntegerEvent.class.getMethod("setVal", Integer.TYPE))),
				asSet(eventFields));
	}

	@Test
	public void canBuildEventFieldsFromSingleStringEventType() throws Exception {
		List<EventField> eventFields = new EventFieldFactory().fromEventType(SingleStringEvent.class);
		assertEquals(
				asSet(new IntegerField(SingleStringEvent.class.getMethod("getVal"), SingleStringEvent.class.getMethod("setVal", String.class))),
				asSet(eventFields));
	}

	@Test
	public void canBuildEventFieldsFromIntegerStringEventType() throws Exception {
		List<EventField> eventFields = new EventFieldFactory().fromEventType(IntegerStringEvent.class);
		assertEquals(
				asSet(new IntegerField(IntegerStringEvent.class.getMethod("getI"), IntegerStringEvent.class.getMethod("setI", Integer.TYPE)),
					  new StringField(IntegerStringEvent.class.getMethod("getS"), IntegerStringEvent.class.getMethod("setS", String.class))),
				asSet(eventFields));
	}

	private Set<EventField> asSet(List<EventField> eventFields) {
		return new HashSet<EventField>(eventFields);
	}

	private Set<EventField> asSet(EventField... fields) {
		return new HashSet<EventField>(Arrays.asList(fields));
	}

}
