package se.jsa.jles.internal.file;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;



public class FlippingEntryFileTest {
	private final FlippingEntryFile fef = new FlippingEntryFile("test.ef", new StreamBasedChannelFactory());

	@After
	public void teardown() {
		fef.close();
		new File("test.ef").delete();
	}

	@Test
	@Ignore
	public void canWriteElement() throws Exception {
		fef.append(ByteBuffer.allocate(1));
		fef.append(ByteBuffer.allocate(1));
	}

	@Test
	public void canCleanup() throws Exception {
		fef.append(ByteBuffer.allocate(1));
		fef.append(ByteBuffer.allocate(1));
		fef.close();
		assertTrue(new File("test.ef").delete());
	}

}
