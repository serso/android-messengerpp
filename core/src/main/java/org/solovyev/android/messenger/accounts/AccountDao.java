package org.solovyev.android.messenger.accounts;

import java.util.Collection;

import javax.annotation.Nonnull;

public interface AccountDao {

	final String TAG = AccountDao.class.getSimpleName();

	void insertAccount(@Nonnull Account account) throws AccountException;

	void deleteAccount(@Nonnull String accountId);

	@Nonnull
	Collection<Account> loadAccounts();

	void deleteAllAccounts();

	void updateAccount(@Nonnull Account account) throws AccountException;

	@Nonnull
	Collection<Account> loadAccountsInState(@Nonnull AccountState state);

	void init();
}
