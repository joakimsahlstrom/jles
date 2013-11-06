package se.jsa.jles.internal.file;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import se.jsa.jles.internal.EntryFile;

public class InMemoryEntryFile implements EntryFile {

	private long position = 0;
	private final Map<Long, ByteBuffer> entries = new HashMap<Long, ByteBuffer>();

	@Override
	public long append(ByteBuffer data) {
		entries.put(position, createCopy(data));
		long index = position;
		position += data.limit();
		return index;
	}

	private ByteBuffer createCopy(ByteBuffer data) {
		data.rewind();
		ByteBuffer copy = ByteBuffer.allocate(data.limit());
		copy.put(data);
		return copy;
	}

	@Override
	public ByteBuffer readEntry(long position) {
		ByteBuffer result = entries.get(position);
		result.rewind();
		return result;
	}

	@Override
	public long size() {
		return position;
	}

	@Override
	public void close() {
		// do nothing
	}

}
