package se.jsa.jles.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.jsa.jles.internal.eventdefinitions.MemoryBasedEventDefinitions;
import se.jsa.jles.internal.file.FlippingEntryFile;
import se.jsa.jles.internal.file.StreamBasedChannelFactory;
import se.jsa.jles.internal.file.SynchronousEntryFile;

@Ignore
@RunWith(value = Parameterized.class)
public class PerformanceTest {

	public static class EmptyEvent {
		// empty by design
	}

	public static class EmptyEvent2 {
		// empty by design
	}

	public static class EmptyEvent3 {
		// empty by design
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

	@Parameters
	public static Collection<Object[]> entryFiles() {
		return Arrays.asList(
				new Object[] { new SynchronousEntryFile("perf.ed") },
				new Object[] { new FlippingEntryFile("perf.ed", new StreamBasedChannelFactory()) }
		);
	}

	private final EntryFile entryFile;

	public PerformanceTest(EntryFile entryFile) {
		this.entryFile = entryFile;
	}

	@After
	public void teardown() {
		entryFile.close();
		new File("perf.ed").delete();
	}

	private final EventDefinitions ed = new MemoryBasedEventDefinitions();

	@Test
	public void measureSimpleWrites() throws Exception {
		System.out.println("\nEntryFile type: " + entryFile.getClass().getSimpleName());
		EventFile ef = new EventFile(entryFile);

		final int COUNT = 100000;
		List<EmptyEvent> events = createEEEvent(COUNT);
		EventSerializer es = ed.getEventSerializer(events.get(0));
		long start = System.nanoTime();
		for (EmptyEvent e : events) {
			ef.writeEvent(e, es);
		}
		long end = System.nanoTime();

		System.out.println("Wrote " + COUNT + " EmptyEvent:s in " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms. " +
				"\nEvents/s = " + (int)((double)COUNT / (end-start) * 1000000000.0));
		System.out.println("Result file size: " + new File("perf.ed").length() + " bytes");
	}

	private List<EmptyEvent> createEEEvent(int num) {
		List<EmptyEvent> result = new ArrayList<EmptyEvent>(num);
		for (int i = 0; i < num; i++) {
			result.add(new EmptyEvent());
		}
		return result;
	}

	@Test
	public void measureWrites() throws Exception {
		EventFile ef = new EventFile(entryFile);

		final int COUNT = 100000;
		List<Object> events = createISEvent(COUNT);
		EventSerializer es = ed.getEventSerializer(events.get(0));
		long start = System.nanoTime();
		for (Object e : events) {
			ef.writeEvent(e, es);
		}
		long end = System.nanoTime();

		System.out.println("Wrote " + COUNT + " IntegerStringEvent:s in " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms. " +
				"\nEvents/s = " + (int)((double)COUNT / (end-start) * 1000000000.0));
		System.out.println("Result file size: " + new File("perf.ed").length() + " bytes");
	}

	public static List<Object> createISEvent(int num) {
		List<Object> result = new ArrayList<Object>(num);
		for (int i = 0; i < num; i++) {
			result.add(new IntegerStringEvent(i, Integer.toString(i)));
		}
		return result;
	}

}
