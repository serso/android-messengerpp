package org.solovyev.android.messenger.sync;

import android.util.Log;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.App.getExceptionHandler;
import static org.solovyev.android.messenger.App.getUserService;
import static org.solovyev.android.messenger.App.newTag;

public enum SyncTask {

	user {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
			if (lastPropertiesSyncDate == null || lastPropertiesSyncDate.plusHours(1).isBefore(DateTime.now())) {
				result = true;
			}

			return result;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			getUserService().syncUser(user.getEntity());
		}
	},

	user_contacts {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			final DateTime lastContactsSyncDate = user.getUserSyncData().getLastContactsSyncDate();
			if (lastContactsSyncDate == null || lastContactsSyncDate.plusHours(1).isBefore(DateTime.now())) {
				result = true;
			}

			return result;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			getUserService().syncUserContacts(user.getEntity());
		}
	},

	user_icons {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			final DateTime lastUserIconsSyncDate = user.getUserSyncData().getLastUserIconsSyncData();
			if (lastUserIconsSyncDate == null || lastUserIconsSyncDate.plusDays(1).isBefore(DateTime.now())) {
				result = true;
			}

			return result;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws UnsupportedAccountException {
			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			getUserService().getIconsService().fetchUserAndContactsIcons(user);
		}
	},

	user_contacts_statuses {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return true;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			getUserService().syncUserContactsStatuses(user.getEntity());
		}
	},

	user_chats {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			final DateTime lastChatsSyncDate = user.getUserSyncData().getLastChatsSyncDate();
			if (lastChatsSyncDate == null || lastChatsSyncDate.plusHours(24).isBefore(DateTime.now())) {
				result = true;
			}

			return result;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			getUserService().syncUserChats(user.getEntity());
		}
	},

	chat_messages {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return true;
		}

		@Override
		protected void doTask0(@Nonnull SyncData syncData) throws AccountException {
			final User user = getAccountService().getAccountById(syncData.getAccountId()).getUser();
			App.getChatService().syncMessages(user.getEntity());
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
}
