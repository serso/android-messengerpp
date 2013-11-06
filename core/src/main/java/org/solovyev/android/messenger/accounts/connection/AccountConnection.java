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

import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 7/25/12
 * Time: 5:52 PM
 */

/**
 * Connection to remote realm.
 * <p/>
 * This class is often used in background and listens to remote events (e.g. implementing long polling).
 * To start listening one must call {@link AccountConnection#start()} method,
 * to finish listening - {@link AccountConnection#stop()}.
 * <p/>
 * Application can toggle state quite often (due, for example, to connectivity problems).
 */
public interface AccountConnection {

	@Nonnull
	Account getAccount();

	/**
	 * Method starts listening to remote realm events
	 */
	void start() throws AccountConnectionException;

	/**
	 * Method stops listening to remove realm events
	 */
	void stop();

	boolean isStopped();

	boolean isInternetConnectionRequired();

	int getRetryCount();
}
