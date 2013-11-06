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

import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.longpoll.LongPollResult;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:47 AM
 */
public class VkLongPollResult implements LongPollResult {

	@Nonnull
	private Long lastUpdate;

	@Nonnull
	private List<LongPollUpdate> updates;

	public VkLongPollResult(@Nonnull Long lastUpdate, @Nonnull List<LongPollUpdate> updates) {
		this.lastUpdate = lastUpdate;
		this.updates = updates;
	}

	@Override
	public Object updateLongPollServerData(@Nullable Object longPollServerData) {
		if (longPollServerData instanceof LongPollServerData) {
			final LongPollServerData lpsd = (LongPollServerData) longPollServerData;
			// NOTE: new timestamp
			return new LongPollServerData(lpsd.getKey(), lpsd.getServerUri(), lastUpdate);
		}

		return longPollServerData;
	}

	@Override
	public void doUpdates(@Nonnull User user, @Nonnull Account account) {
		for (LongPollUpdate update : updates) {
			try {
				update.doUpdate(user, account);
			} catch (AccountException e) {
				App.getExceptionHandler().handleException(e);
			}
		}
	}
}
