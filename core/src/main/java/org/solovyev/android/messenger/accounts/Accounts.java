package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Accounts {

	private Accounts() {
	}

	@Nonnull
	public static AccountSyncData newNeverSyncedData() {
		return new AccountSyncDataImpl(null, null, null);
	}

	@Nonnull
	public static AccountSyncData newUserSyncData(@Nullable String lastContactsSyncDate,
												  @Nullable String lastChatsSyncDate,
												  @Nullable String lastUserIconsSyncDate) {
		return AccountSyncDataImpl.newInstance(lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	@Nonnull
	public static Runnable withAccountException(@Nonnull final AccountRunnable runnable) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (AccountException e) {
					throw new AccountRuntimeException(e);
				}
			}
		};
	}
}
