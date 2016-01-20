/*
 * Copyright 2016 Joakim Sahlimport static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.testevents.ObjectTestEvent;
ONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.jsa.jles.internal;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.testevents.ObjectTestEvent;

public class EventFileTest {

	public static class EmptyEvent {
		// empty by design
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EmptyEvent) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "EmptyEvent []";
		}
	}

	public static class EmptyEvent2 {
		// empty by design
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EmptyEvent2) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "EmptyEvent2 []";
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleIntegerEvent) {
				return val == ((SingleIntegerEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleIntegerEvent [val=" + val + "]";
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

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleStringEvent) {
				return val.equals(((SingleStringEvent)obj).val);
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleStringEvent [val=" + val + "]";
		}
	}

	public static class SingleBooleanEvent {
		public boolean val;

		public SingleBooleanEvent() {
		}

		public SingleBooleanEvent(boolean val) {
			this.val = val;
		}

		public boolean getVal() {
			return val;
		}

		public void setVal(boolean val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleBooleanEvent) {
				return val == ((SingleBooleanEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleBooleanEvent [val=" + val + "]";
		}
	}

	public static class SingleByteEvent {
		public byte val;

		public SingleByteEvent() {
		}

		public SingleByteEvent(byte val) {
			this.val = val;
		}

		public byte getVal() {
			return val;
		}

		public void setVal(byte val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleByteEvent) {
				return val == ((SingleByteEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleByteEvent [val=" + val + "]";
		}
	}

	public static class SingleShortEvent {
		public short val;

		public SingleShortEvent() {
		}

		public SingleShortEvent(short val) {
			this.val = val;
		}

		public short getVal() {
			return val;
		}

		public void setVal(short val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleShortEvent) {
				return val == ((SingleShortEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleShortEvent [val=" + val + "]";
		}
	}

	public static class SingleLongEvent {
		public long val;

		public SingleLongEvent() {
		}

		public SingleLongEvent(long val) {
			this.val = val;
		}

		public long getVal() {
			return val;
		}

		public void setVal(long val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleLongEvent) {
				return val == ((SingleLongEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleLongEvent [val=" + val + "]";
		}
	}

	public static class SingleFloatEvent {
		public float val;

		public SingleFloatEvent() {
		}

		public SingleFloatEvent(float val) {
			this.val = val;
		}

		public float getVal() {
			return val;
		}

		public void setVal(float val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleFloatEvent) {
				return val == ((SingleFloatEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleFloatEvent [val=" + val + "]";
		}
	}

	public static class SingleDoubleEvent {
		public double val;

		public SingleDoubleEvent() {
		}

		public SingleDoubleEvent(double val) {
			this.val = val;
		}

		public double getVal() {
			return val;
		}

		public void setVal(double val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleDoubleEvent) {
				return val == ((SingleDoubleEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleDoubleEvent [val=" + val + "]";
		}
	}

	public static class SingleCharEvent {
		public char val;

		public SingleCharEvent() {
		}

		public SingleCharEvent(char val) {
			this.val = val;
		}

		public char getVal() {
			return val;
		}

		public void setVal(char val) {
			this.val = val;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SingleCharEvent) {
				return val == ((SingleCharEvent)obj).val;
			}
			return false;
		}

		@Override
		public String toString() {
			return "SingleCharEvent [val=" + val + "]";
		}
	}

	public static class MultipleFieldsEvent {
		private byte b;
		private short s;
		private int i;
		private long l;
		private float f;
		private double d;
		private boolean B;
		private char c;
		private String S;

		public MultipleFieldsEvent() {
			// for deserialization
		}

		public MultipleFieldsEvent(byte b, short s, int i, long l, float f, double d, boolean B, char c, String S) {
			super();
			this.b = b;
			this.s = s;
			this.i = i;
			this.l = l;
			this.f = f;
			this.d = d;
			this.B = B;
			this.c = c;
			this.S = S;
		}

		public byte getB() {
			return b;
		}

		public void setB(byte b) {
			this.b = b;
		}

		public short getS() {
			return s;
		}

		public void setS(short s) {
			this.s = s;
		}

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}

		public long getL() {
			return l;
		}

		public void setL(long l) {
			this.l = l;
		}

		public float getF() {
			return f;
		}

		public void setF(float f) {
			this.f = f;
		}

		public double getD() {
			return d;
		}

		public void setD(double d) {
			this.d = d;
		}

		public boolean getBB() {
			return B;
		}

		public void setBB(boolean b) {
			this.B = b;
		}

		public char getC() {
			return c;
		}

		public void setC(char c) {
			this.c = c;
		}

		public String getSS() {
			return S;
		}

		public void setSS(String s) {
			S = s;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MultipleFieldsEvent) {
				MultipleFieldsEvent other = (MultipleFieldsEvent) obj;
				return this.b == other.b
						&& this.s == other.s
						&& this.i == other.i
						&& this.l == other.l
						&& this.f == other.f
						&& this.d == other.d
						&& this.B == other.B
						&& this.S.equals(other.S);
			}
			return false;
		}

		@Override
		public String toString() {
			return "MultipleFieldsEvent [b=" + b + ", s=" + s + ", i=" + i
					+ ", l=" + l + ", f=" + f + ", d=" + d + ", B=" + B
					+ ", c=" + c + ", S=" + S + "]";
		}

	}

	private final EventFile eventFile = new EventFile(new FlippingEntryFile("test.ef", new StreamBasedChannelFactory()));

	@After
	public void teardown() {
		eventFile.close();
		new File("test.ef").delete();
	}

	@Test
	public void eventTypesCanBeSerializedAndDeserialized() throws Exception {
		List<Object> events = Arrays.asList(
				new EmptyEvent(),
				new EmptyEvent2(),
				new SingleIntegerEvent(1),
				new SingleIntegerEvent(2),
				new SingleStringEvent("test"),
				new SingleBooleanEvent(false),
				new SingleBooleanEvent(true),
				new SingleByteEvent((byte)27),
				new SingleShortEvent((short)3077),
				new SingleLongEvent(10000023232L),
				new SingleFloatEvent(1.21f),
				new SingleDoubleEvent(1.21d),
				new SingleCharEvent('ö'),
				new MultipleFieldsEvent((byte)3, (short)290, 32767, 100000003L, 1.1f, 1.1212d, true, 'G', "sträng"));

		EventDefinitions ed = new MemoryBasedEventDefinitions();
		for (Object event : events) {
			EventSerializer eventSerializer = ed.getEventSerializer(event);
			long position = eventFile.writeEvent(event, eventSerializer);
			Object output = eventFile.readEvent(position, ed.getEventDeserializer(eventSerializer.getEventTypeId()));
			assertEquals(event, output);
		}
	}

	@Test
	public void canWriteObjectVersionsOfPrimitives() throws Exception {
		ObjectTestEvent event = new ObjectTestEvent("name", 1L, Boolean.TRUE);

		EventDefinitions ed = new MemoryBasedEventDefinitions();
		EventSerializer eventSerializer = ed.getEventSerializer(event);
		long position = eventFile.writeEvent(event, eventSerializer);
		Object output = eventFile.readEvent(position, ed.getEventDeserializer(eventSerializer.getEventTypeId()));
		assertEquals(event, output);
	}

	@Test
	public void canWriteNullEventField() throws Exception {
		ObjectTestEvent event = new ObjectTestEvent(null, null, Boolean.TRUE);

		EventDefinitions ed = new MemoryBasedEventDefinitions();
		EventSerializer eventSerializer = ed.getEventSerializer(event);
		long position = eventFile.writeEvent(event, eventSerializer);
		Object output = eventFile.readEvent(position, ed.getEventDeserializer(eventSerializer.getEventTypeId()));
		assertEquals(event, output);
	}

}
