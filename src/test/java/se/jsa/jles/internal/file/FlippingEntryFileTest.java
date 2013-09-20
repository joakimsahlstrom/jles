package se.jsa.jles.internal.file;

import java.io.File;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import se.jsa.jles.internal.file.FlippingEntryFile;



public class FlippingEntryFileTest {

	@After
	@Before
	public void teardown() {
		new File("test.ef").delete();
	}

	@Test
	public void canWriteElement() throws Exception {
		FlippingEntryFile fef = new FlippingEntryFile("test.ef", new StreamBasedChannelFactory());
		fef.append(ByteBuffer.allocate(1));
		fef.append(ByteBuffer.allocate(1));
	}

}
