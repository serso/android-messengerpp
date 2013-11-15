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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.solovyev.android.messenger.accounts.Account;

@ThreadSafe
public interface AccountConnections {

	void startConnectionsFor(@Nonnull Collection<Account> accounts, boolean internetConnectionExists);

	void tryStopAll();

	boolean onNoInternetConnection();

	void tryStopFor(@Nonnull Account account);

	void tryStartAll(boolean internetConnectionExists);

	void removeConnectionFor(@Nonnull Account account);

	void updateAccount(@Nonnull Account account, boolean internetConnectionExists);
}
