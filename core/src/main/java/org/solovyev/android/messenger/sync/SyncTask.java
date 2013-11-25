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

package org.solovyev.android.messenger.sync;

import android.util.Log;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;

import javax.annotation.Nonnull;

import static org.solovyev.android.messenger.App.*;

public enum SyncTask {

	user_contacts {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			final Account account = getAccountService().getAccountById(syncData.getAccountId());
			final DateTime lastContactsSyncDate = account.getSyncData().getLastContactsSyncDate();
			if (lastContactsSyncDate == null || lastContactsSyncDate.plusHours(1).isBefore(DateTime.now())) {
				result = true;
			}

			return result;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			getUserService().syncContacts(getAccountService().getAccountById(syncData.getAccountId()));
		}
	},

	user_icons {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			final Account account = getAccountService().getAccountById(syncData.getAccountId());
			final DateTime lastUserIconsSyncDate = account.getSyncData().getLastUserIconsSyncData();
			if (lastUserIconsSyncDate == null || lastUserIconsSyncDate.plusDays(1).isBefore(DateTime.now())) {
				result = true;
			}

			return result;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws UnsupportedAccountException {
			final Account account = getAccountService().getAccountById(syncData.getAccountId());
			getUserService().getIconsService().fetchUserAndContactsIcons(account);
		}
	},

	user_contacts_statuses {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return true;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final Account account = getAccountService().getAccountById(syncData.getAccountId());
			getUserService().syncContactStatuses(account);
		}
	},

	user_chats {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return isTimeForChatsUpdate(syncData);
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final Account account = getAccountService().getAccountById(syncData.getAccountId());
			getUserService().syncChats(account);
		}
	},

	chat_messages {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return isTimeForChatsUpdate(syncData);
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			App.getChatService().syncMessages(getAccountService().getAccountById(syncData.getAccountId()));
		}
	};

	private static final String TAG = newTag("SyncTask");

	@Nonnull
	private static AccountService getAccountService() {
		return App.getAccountService();
	}

	public abstract boolean isTime(@Nonnull SyncData syncData);

	public final void doTask(@Nonnull SyncData syncData) {
		logTaskStarted(syncData);

		try {
			doTask0(syncData);
		} catch (AccountException e) {
			// ok, user is not logged in
			getExceptionHandler().handleException(e);
		} finally {
			logTaskFinished(syncData);
		}
	}

	protected abstract void doTask0(@Nonnull SyncData syncData) throws AccountException;

	protected void logTaskStarted(@Nonnull SyncData syncData) {
		Log.i(TAG, "Sync task started: " + this + " for account: " + syncData.getAccountId());
	}

	private void logTaskFinished(@Nonnull SyncData syncData) {
		Log.i(TAG, "Sync task finished: " + this + " for account: " + syncData.getAccountId());
	}

	private static boolean isTimeForChatsUpdate(SyncData syncData) {
		boolean result = false;

		final Account account = getAccountService().getAccountById(syncData.getAccountId());
		final DateTime lastChatsSyncDate = account.getSyncData().getLastChatsSyncDate();
		if (lastChatsSyncDate == null || lastChatsSyncDate.plusHours(1).isBefore(DateTime.now())) {
			result = true;
		}

		return result;
	}
}
