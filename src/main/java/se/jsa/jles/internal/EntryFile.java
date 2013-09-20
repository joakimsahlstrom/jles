package se.jsa.jles.internal;

import java.nio.ByteBuffer;

public interface EntryFile {

	public long append(ByteBuffer data);
	public ByteBuffer readEntry(long position);
	public long size();

	public void close();

}
