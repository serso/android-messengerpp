package org.solovyev.android.messenger.accounts;

import org.solovyev.android.db.Dao;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface AccountDao extends Dao<Account> {

	final String TAG = AccountDao.class.getSimpleName();

	void init();

	long create(@Nonnull Account account) throws AccountRuntimeException;

	void deleteAll();

	long update(@Nonnull Account account) throws AccountRuntimeException;

	@Nonnull
	Collection<Account> loadAccountsInState(@Nonnull AccountState state);
}
