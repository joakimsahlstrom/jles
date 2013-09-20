package se.jsa.jles.internal.file;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class EntryFileSerializer {

	public ByteBuffer readEntry(long position, FileChannel channel) throws IOException {
		int size = readEntrySize(position, channel);

		ByteBuffer result = ByteBuffer.allocate(8 + 4 + size);
		channel.position(position);
		channel.read(result);
		result.rewind();
		return result;
	}

	private int readEntrySize(long position, FileChannel channel) throws IOException {
		ByteBuffer eventSize = ByteBuffer.allocate(4);
		channel.position(position + 8);
		channel.read(eventSize);
		eventSize.rewind();
		return eventSize.getInt();
	}

	public long append(ByteBuffer data, FileChannel channel) throws IOException {
		long position = channel.size();

		data.rewind();
		channel.write(data);
		return position;
	}

}
