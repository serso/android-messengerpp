package org.solovyev.android;

import android.os.SystemClock;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class MessengerExecutor implements Executor {

	private static final String TAG = MessengerExecutor.class.getSimpleName();

	private static final long MAX_WAIT_MILLIS = 100;
	private static final long MAX_WORK_MILLIS = 100;

	private final Executor executor = Executors.newSingleThreadExecutor();

	@Override
	public void execute(final Runnable command) {
		final StringWriter stringWriter = new StringWriter();
		new Throwable().printStackTrace(new PrintWriter(stringWriter));
		final String stackTrace = stringWriter.toString();

		final long addedToQueueTime = SystemClock.elapsedRealtime();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final long startTime = SystemClock.elapsedRealtime();
				try {
					command.run();
				} finally {
					final long endTime = SystemClock.elapsedRealtime();
					final long waitMillis = startTime - addedToQueueTime;
					if (waitMillis > MAX_WAIT_MILLIS) {
						Log.e(TAG, "Wait time is too long at " + stackTrace);
					}
					final long workMillis = endTime - startTime;
					if (workMillis > MAX_WORK_MILLIS) {
						Log.e(TAG, "Work time is too long at " + stackTrace);
					}
				}
			}
		});
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return executor.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return executor.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return executor.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return executor.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executor.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return executor.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return executor.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return executor.invokeAny(tasks, timeout, unit);
	}
}
