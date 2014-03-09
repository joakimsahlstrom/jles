package se.jsa.jles.internal.file;

import java.nio.ByteBuffer;
import java.util.WeakHashMap;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.util.Objects;

public class CachingEntryFile implements EntryFile {

	private final EntryFile file;
	private final WeakHashMap<Long, ByteBuffer> cache = new WeakHashMap<Long, ByteBuffer>();

	public CachingEntryFile(EntryFile file) {
		this.file = Objects.requireNonNull(file);
	}

	@Override
	public long append(ByteBuffer data) {
		return file.append(data);
	}

	@Override
	public ByteBuffer readEntry(long position) {
		ByteBuffer result = cache.get(position);
		if (result != null) {
			result.rewind();
			return result;
		}

		result = file.readEntry(position);
		cache.put(position, result);
		return result;
	}

	@Override
	public long size() {
		return file.size();
	}

	@Override
	public void close() {
		file.close();
	}

}