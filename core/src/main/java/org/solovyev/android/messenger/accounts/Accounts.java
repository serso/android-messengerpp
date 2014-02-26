package org.solovyev.android.messenger.accounts;

import android.content.Context;
import android.os.Bundle;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.users.BaseEditUserFragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public final class Accounts {

	@Nonnull
	private static final String ARG_ACCOUNT_ID = "account_id";

	@Nonnull
	private static final String ARG_EDIT_CLASS_NAME = "edit_class_name";

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

	@Nonnull
	public static Bundle newAccountArguments(@Nonnull Account account) {
		final Bundle result = new Bundle();
		result.putString(ARG_ACCOUNT_ID, account.getId());
		return result;
	}

	private static void putEditAccountFragmentClass(@Nonnull Account account, @Nonnull Bundle arguments) {
		final Class fragmentClass = account.getRealm().getConfigurationFragmentClass();
		arguments.putSerializable(ARG_EDIT_CLASS_NAME, fragmentClass);
	}

	@Nonnull
	public static Bundle newEditAccountArguments(@Nonnull Account account) {
		final Bundle result = new Bundle();
		result.putString(ARG_ACCOUNT_ID, account.getId());
		putEditAccountFragmentClass(account, result);
		return result;
	}

	@Nullable
	public static String getAccountIdFromArguments(@Nonnull Bundle arguments) {
		return arguments.getString(Accounts.ARG_ACCOUNT_ID);
	}

	@Nullable
	public static Class<? extends BaseAccountFragment> getEditAccountFragmentClassFromArguments(@Nonnull Bundle arguments) {
		final Serializable serializable = arguments.getSerializable(ARG_EDIT_CLASS_NAME);
		if (serializable instanceof Class) {
			return (Class) serializable;
		} else {
			return null;
		}
	}

	@Nonnull
	public static String getAccountName(@Nonnull Account account) {
		return getAccountName(App.getApplication(), App.getAccountService(), account);
	}

	@Nonnull
	public static String getAccountName(@Nonnull Context context, @Nonnull AccountService accountService, @Nonnull Account account) {
		if (accountService.isOneAccount(account.getRealm())) {
			return account.getDisplayName(context);
		} else {
			return account.getDisplayName(context) + "/" + account.getUser().getDisplayName();
		}
	}
}
