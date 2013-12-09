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

import javax.annotation.Nonnull;

import org.solovyev.android.async.CommonAsyncTask;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.Background;

import static org.solovyev.android.messenger.App.getExceptionHandler;

public abstract class MessengerAsyncTask<Param, Progress, R> extends CommonAsyncTask<Param, Progress, R> {

	private final boolean lowPriority;

	protected MessengerAsyncTask() {
		this(false);
	}

	protected MessengerAsyncTask(boolean lowPriority) {
		this.lowPriority = lowPriority;
	}

	protected MessengerAsyncTask(@Nonnull Context context) {
		this(context, false);
	}

	protected MessengerAsyncTask(@Nonnull Context context, boolean lowPriority) {
		super(context);
		this.lowPriority = lowPriority;
	}

	@Override
	protected void onFailurePostExecute(@Nonnull Exception e) {
		getExceptionHandler().handleException(e);
	}

	@Nonnull
	public final AsyncTask<Param, Progress, Result<R>> executeInParallel(Param... params) {
		return executeInParallel(this, params);
	}

	public static <Param, Progress, R> AsyncTask<Param, Progress, Result<R>> executeInParallel(@Nonnull AsyncTask<Param, Progress, Result<R>> task, Param... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final Background background = App.getBackground();

			final boolean lowPriority;
			if (task instanceof MessengerAsyncTask) {
				lowPriority = ((MessengerAsyncTask) task).lowPriority;
			} else {
				lowPriority = false;
			}

			task.executeOnExecutor(lowPriority ? background.getLowPriorityExecutor() : background.getHighPriorityExecutor(), params);
			return task;
		} else {
			task.execute(params);
			return task;
		}
	}
}
