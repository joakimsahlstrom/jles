package se.jsa.jles.internal.file;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import se.jsa.jles.internal.EntryFile;
import se.jsa.jles.internal.util.Objects;

/**
 * Wraps other {@link EntryFile}s to make all interaction with them thread safe
 * @author joakim Joakim Sahlström
 *
 */
public class ThreadSafeEntryFile implements EntryFile {
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final EntryFile underlyingFile;

	public ThreadSafeEntryFile(EntryFile underlyingFile) {
		this.underlyingFile = Objects.requireNonNull(underlyingFile);
	}

	@Override
	public long append(final ByteBuffer data) {
		Callable<Long> command = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return getUnderlyingFile().append(data);
			}
		};
		return run(command);
	}

	@Override
	public ByteBuffer readEntry(final long position) {
		Callable<ByteBuffer> command = new Callable<ByteBuffer>() {
			@Override
			public ByteBuffer call() throws Exception {
				return getUnderlyingFile().readEntry(position);
			}
		};
		return run(command);
	}

	@Override
	public long size() {
		Callable<Long> command = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return getUnderlyingFile().size();
			}
		};
		return run(command);
	}

	@Override
	public void close() {
		Callable<Void> command = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				getUnderlyingFile().close();
				return null;
			}
		};
		run(command);
		executor.shutdown();
	}

	protected final EntryFile getUnderlyingFile() {
		return underlyingFile;
	}

	private <T> T run(Callable<T> command) {
		Future<T> result = executor.submit(command);
		try {
			return result.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted"); // for compiler, above call throws
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else {
				throw new RuntimeException("Could not execute append command", cause);
			}
		}
	}

	@Override
	public String toString() {
		return "NonBlockingEntryFile [executor=" + executor + ", underlyingFile=" + getUnderlyingFile() + "]";
	}
}
