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

package org.solovyev.android.messenger.notifications;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.common.msg.AbstractMessage;
import org.solovyev.common.msg.MessageLevel;

import static org.solovyev.android.messenger.App.getApplication;
import static org.solovyev.android.messenger.App.getNotificationService;

public final class Notification extends AbstractMessage {

	@Nullable
	private NotificationSolution solution;
	@Nullable
	private Throwable cause;

	private Notification(int messageResId, @Nonnull MessageLevel messageType, @Nullable Object... parameters) {
		super(String.valueOf(messageResId), messageType, parameters);
	}

	private Notification(int messageResId, @Nonnull MessageLevel messageType, @Nonnull List<?> parameters) {
		super(String.valueOf(messageResId), messageType, parameters);
	}

	@Nonnull
	static Notification newInstance(int messageResId, @Nonnull MessageLevel messageType, @Nullable Object... parameters) {
		return new Notification(messageResId, messageType, parameters);
	}

	@Nonnull
	static Notification newInstance(int messageResId, @Nonnull MessageLevel messageType, @Nonnull List<?> parameters) {
		return new Notification(messageResId, messageType, parameters);
	}

	@Nullable
	@Override
	protected String getMessagePattern(@Nonnull Locale locale) {
		final int messageResId = Integer.valueOf(getMessageCode());
		final List<Object> parameters = getParameters();
		return getApplication().getString(messageResId, parameters.toArray(new Object[parameters.size()]));
	}

	public void solveOnClick() {
		if (solution != null) {
			solution.solve(this);
		} else {
			getNotificationService().remove(this);
		}
	}

	@Nonnull
	public Notification solvedBy(@Nullable NotificationSolution solution) {
		this.solution = solution;

		return this;
	}

	@Nonnull
	public Notification causedBy(@Nullable Throwable cause) {
		this.cause = cause;
		if (this.cause != null) {
			if (this.solution == null) {
				this.solution = Notifications.NotifyDeveloperSolution.getInstance();
			}
		}

		return this;
	}

	@Nullable
	Throwable getCause() {
		return cause;
	}

	public void dismiss() {
		getNotificationService().remove(this);
	}
}
