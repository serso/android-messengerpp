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

package org.solovyev.android.messenger.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import org.solovyev.android.async.CommonAsyncTask;
import org.solovyev.android.messenger.App;

import javax.annotation.Nonnull;

import static java.lang.System.currentTimeMillis;
import static org.solovyev.android.messenger.App.getExceptionHandler;

public abstract class MessengerAsyncTask<Param, Progress, R> extends CommonAsyncTask<Param, Progress, R> {

	private long startTime = 0;

	protected MessengerAsyncTask() {
	}

	protected MessengerAsyncTask(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected void onFailurePostExecute(@Nonnull Exception e) {
		getExceptionHandler().handleException(e);
	}

	/*@Override
	protected void onPostExecute(@Nonnull Result<R> r) {
		final long endTime = currentTimeMillis();
		final long workMillis = endTime - startTime;
		if (workMillis > 100) {
			Log.e(App.TAG_TIME, "Work time is too long (" + workMillis + " ms) for " + getClass().getSimpleName() + " (" + this + ")");
		}
		super.onPostExecute(r);
	}*/

	@Nonnull
	public final AsyncTask<Param, Progress, Result<R>> executeInParallel(Param... params) {
		startTime = currentTimeMillis();
		return executeInParallel(this, params);
	}

	public static <Param, Progress, R> AsyncTask<Param, Progress, Result<R>> executeInParallel(@Nonnull AsyncTask<Param, Progress, Result<R>> task, Param... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			return task.execute(params);
		}
	}
}
