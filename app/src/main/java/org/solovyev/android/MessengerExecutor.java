package org.solovyev.android;

import android.util.Log;
import com.google.inject.Singleton;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.os.SystemClock.elapsedRealtime;
import static org.solovyev.android.messenger.App.newTag;

@Singleton
public final class MessengerExecutor implements Executor {

	private static final String TAG = newTag("Executor");

	private static final long MAX_WAIT_MILLIS = 100;
	private static final long MAX_WORK_MILLIS = 100;

	private final Executor executor = Executors.newSingleThreadExecutor();

	@Override
	public void execute(@Nonnull final Runnable command) {
		final StringWriter stringWriter = new StringWriter();
		new Throwable().printStackTrace(new PrintWriter(stringWriter));
		final String stackTrace = stringWriter.toString();

		final long addedToQueueTime = elapsedRealtime();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final long startTime = elapsedRealtime();
				try {
					command.run();
				} finally {
					final long endTime = elapsedRealtime();
					final long waitMillis = startTime - addedToQueueTime;
					if (waitMillis > MAX_WAIT_MILLIS) {
						Log.e(TAG, "Wait time is too long (" + waitMillis + " ms) at " + stackTrace);
					}
					final long workMillis = endTime - startTime;
					if (workMillis > MAX_WORK_MILLIS) {
						Log.e(TAG, "Work time is too long (" + workMillis + " ms) at " + stackTrace);
					}
				}
			}
		});
	}
}
