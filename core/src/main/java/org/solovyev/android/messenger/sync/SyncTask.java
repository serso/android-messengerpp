package org.solovyev.android.messenger.sync;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.users.User;

import static org.solovyev.android.messenger.App.getExceptionHandler;
import static org.solovyev.android.messenger.App.getUserService;

/**
 * User: serso
 * Date: 5/30/12
 * Time: 11:18 PM
 */
public enum SyncTask {

	user {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				final DateTime lastPropertiesSyncDate = user.getUserSyncData().getLastPropertiesSyncDate();
				if (lastPropertiesSyncDate == null || lastPropertiesSyncDate.plusHours(1).isBefore(DateTime.now())) {
					result = true;
				}
			} catch (UnsupportedAccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}

			return result;
		}

		@Override
		public void doTask(@Nonnull SyncData syncData) {
			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				getUserService().syncUser(user.getEntity());
			} catch (AccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}
		}
	},

	user_contacts {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				final DateTime lastContactsSyncDate = user.getUserSyncData().getLastContactsSyncDate();
				if (lastContactsSyncDate == null || lastContactsSyncDate.plusHours(1).isBefore(DateTime.now())) {
					result = true;
				}
			} catch (UnsupportedAccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}

			return result;
		}

		@Override
		public void doTask(@Nonnull SyncData syncData) {
			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				getUserService().syncUserContacts(user.getEntity());
			} catch (AccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}
		}
	},

	user_icons {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				final DateTime lastUserIconsSyncDate = user.getUserSyncData().getLastUserIconsSyncData();
				if (lastUserIconsSyncDate == null || lastUserIconsSyncDate.plusDays(1).isBefore(DateTime.now())) {
					result = true;
				}
			} catch (UnsupportedAccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}

			return result;
		}

		@Override
		public void doTask(@Nonnull SyncData syncData) {
			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();

				getUserService().fetchUserAndContactsIcons(user);

			} catch (UnsupportedAccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}
		}
	},

	user_contacts_statuses {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return true;
		}

		@Override
		public void doTask(@Nonnull SyncData syncData) {
			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				getUserService().syncUserContactsStatuses(user.getEntity());
			} catch (AccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}
		}
	},

	user_chats {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			boolean result = false;

			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				final DateTime lastChatsSyncDate = user.getUserSyncData().getLastChatsSyncDate();
				if (lastChatsSyncDate == null || lastChatsSyncDate.plusHours(24).isBefore(DateTime.now())) {
					result = true;
				}
			} catch (UnsupportedAccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}

			return result;
		}

		@Override
		public void doTask(@Nonnull SyncData syncData) {
			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				getUserService().syncUserChats(user.getEntity());
			} catch (AccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}
		}
	},

	chat_messages {
		@Override
		public boolean isTime(@Nonnull SyncData syncData) {
			return true;
		}

		@Override
		public void doTask(@Nonnull SyncData syncData) {
			try {
				final User user = getRealmService().getAccountById(syncData.getRealmId()).getUser();
				App.getChatService().syncChatMessages(user.getEntity());
			} catch (AccountException e) {
				// ok, user is not logged in
				getExceptionHandler().handleException(e);
			}
		}
	};

	@Nonnull
	private static AccountService getRealmService() {
		return App.getAccountService();
	}

	public abstract boolean isTime(@Nonnull SyncData syncData);

	public abstract void doTask(@Nonnull SyncData syncData);
}
