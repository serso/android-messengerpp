/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android;

import android.util.Log;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.App;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.os.SystemClock.elapsedRealtime;

@Singleton
public final class TimeLoggingExecutor implements Executor {

	private static final long MAX_WAIT_MILLIS = 100;
	private static final long MAX_WORK_MILLIS = 100;

	private final Executor executor = Executors.newSingleThreadExecutor();

	@Override
	public void execute(@Nonnull final Runnable command) {
		/*final StringWriter stringWriter = new StringWriter();
		new Throwable().printStackTrace(new PrintWriter(stringWriter));
		final String stackTrace = stringWriter.toString();*/

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
						Log.e(App.TAG_TIME, "Wait time is too long (" + waitMillis + " ms) for " + command.getClass().getSimpleName());
					}
					final long workMillis = endTime - startTime;
					if (workMillis > MAX_WORK_MILLIS) {
						Log.e(App.TAG_TIME, "Work time is too long (" + workMillis + " ms) for " + command.getClass().getSimpleName());
					}
				}
			}
		});
	}
}
