package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface AccountDao {

	final String TAG = AccountDao.class.getSimpleName();

	void insertRealm(@Nonnull Account account) throws AccountException;

	void deleteRealm(@Nonnull String realmId);

	@Nonnull
	Collection<Account> loadRealms();

	void deleteAllRealms();

	void updateRealm(@Nonnull Account account) throws AccountException;

	@Nonnull
	Collection<Account> loadRealmsInState(@Nonnull AccountState state);

	void init();
}
