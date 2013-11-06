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

package org.solovyev.android.messenger.realms.vk.longpoll;

import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.longpoll.LongPollResult;
import org.solovyev.android.messenger.longpoll.RealmLongPollService;
import org.solovyev.android.messenger.realms.vk.VkAccount;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:19 AM
 */
public class VkRealmLongPollService implements RealmLongPollService {

	@Nonnull
	private final VkAccount account;

	public VkRealmLongPollService(@Nonnull VkAccount account) {
		this.account = account;
	}

	@Override
	public Object startLongPolling() throws AccountException {
		try {
			return HttpTransactions.execute(new VkGetLongPollServerHttpTransaction(account));
		} catch (HttpRuntimeIoException e) {
			throw new AccountException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountException(account.getId(), e);
		}
	}

	@Override
	public LongPollResult waitForResult(@Nullable Object longPollingData) throws AccountException {
		try {
			if (longPollingData instanceof LongPollServerData) {
				return HttpTransactions.execute(new VkGetLongPollingDataHttpTransaction((LongPollServerData) longPollingData));
			} else {
				return null;
			}
		} catch (HttpRuntimeIoException e) {
			throw new AccountException(account.getId(), e);
		} catch (IOException e) {
			throw new AccountException(account.getId(), e);
		}
	}
}
