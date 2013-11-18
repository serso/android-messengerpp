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

package org.solovyev.android.messenger.accounts.connection;

import android.content.Context;
import android.util.Log;

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.accounts.DefaultAccountService;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.solovyev.android.messenger.App.getSyncService;
import static org.solovyev.android.messenger.App.newTag;


public abstract class AbstractAccountConnection<A extends Account> implements AccountConnection {

	@Nonnull
	protected final String TAG = newTag(getClass().getName());

	private static final int DEFAULT_RETRY_COUNT = 5;

	@Nonnull
	private volatile A account;

	@Nonnull
	private final Context context;

	@Nonnull
	private final AtomicBoolean stopped = new AtomicBoolean(true);

	private final boolean internetConnectionRequired;

	private final int retryCount;

	protected AbstractAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired) {
		this(account, context, internetConnectionRequired, DEFAULT_RETRY_COUNT);
	}

	protected AbstractAccountConnection(@Nonnull A account, @Nonnull Context context, boolean internetConnectionRequired, int retryCount) {
		this.account = account;
		this.context = context;
		this.internetConnectionRequired = internetConnectionRequired;
		this.retryCount = retryCount;
	}

	@Nonnull
	public final A getAccount() {
		return account;
	}

	@Nonnull
	protected Context getContext() {
		return context;
	}

	public boolean isStopped() {
		return stopped.get();
	}

	@Override
	public final void start() throws AccountConnectionException {
		stopped.compareAndSet(true, false);

		getSyncService().waitWhileSyncFinished();

		start0();
	}

	protected abstract void start0() throws AccountConnectionException;

	protected abstract void stop0();

	@Override
	public final void stop() {
		if (stopped.compareAndSet(false, true)) {
			Log.d(TAG, "Trying to stop connection");
			stop0();
		} else {
			Log.d(TAG, "Connection is already stopped");
		}
	}

	@Override
	public boolean isInternetConnectionRequired() {
		return internetConnectionRequired;
	}

	@Override
	public int getRetryCount() {
		return retryCount;
	}
}
