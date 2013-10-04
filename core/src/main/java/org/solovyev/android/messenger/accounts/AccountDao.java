package org.solovyev.android.messenger.accounts;

import javax.annotation.Nonnull;
import java.util.Collection;

import org.solovyev.android.db.Dao;

public interface AccountDao extends Dao<Account> {

	final String TAG = AccountDao.class.getSimpleName();

	void init();

	void create(@Nonnull Account account) throws AccountRuntimeException;

	void deleteAllAccounts();

	void update(@Nonnull Account account) throws AccountRuntimeException;

	@Nonnull
	Collection<Account> loadAccountsInState(@Nonnull AccountState state);
}
