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

package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.ProgressDialog;
import org.solovyev.android.tasks.ContextCallback;
import org.solovyev.android.tasks.Tasks;
import org.solovyev.tasks.NoSuchTaskException;
import org.solovyev.tasks.TaskFinishedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 11:01 PM
 */
public final class TaskOverlayDialog<V> implements ContextCallback<Activity, V> {

	@Nonnull
	private final ProgressDialog progressDialog;

	private volatile boolean finished = false;

	private TaskOverlayDialog(@Nonnull ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	@Nonnull
	private static <V> TaskOverlayDialog<V> newInstance(@Nonnull Activity activity, int titleResId, int messageResId) {
		final ProgressDialog progressDialog = new ProgressDialog(activity);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.setTitle(titleResId);
		progressDialog.setMessage(activity.getText(messageResId));
		return new TaskOverlayDialog<V>(progressDialog);
	}

	@Nullable
	public static TaskOverlayDialog<?> attachToTask(@Nonnull Activity activity, @Nonnull String taskName, int titleResId, int messageResId) {
		TaskOverlayDialog<Object> taskOverlayDialog = newInstance(activity, titleResId, messageResId);
		try {
			App.getTaskService().tryAddTaskListener(taskName, Tasks.toFutureCallback(activity, taskOverlayDialog));
			// attached to task => can show dialog
			taskOverlayDialog.show();
		} catch (NoSuchTaskException e) {
			taskOverlayDialog = null;
		} catch (TaskFinishedException e) {
			taskOverlayDialog = null;
		}

		return taskOverlayDialog;
	}


	@Override
	public void onSuccess(@Nonnull Activity context, V result) {
		dismiss();
	}

	public synchronized void dismiss() {
		finished = true;
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	public synchronized void show() {
		if (!finished) {
			progressDialog.show();
		}
	}

	@Override
	public void onFailure(@Nonnull Activity context, Throwable t) {
		dismiss();
	}
}
