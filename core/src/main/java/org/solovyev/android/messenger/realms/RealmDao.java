package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface RealmDao {

	final String TAG = RealmDao.class.getSimpleName();

	void insertRealm(@Nonnull Account account) throws RealmException;

	void deleteRealm(@Nonnull String realmId);

	@Nonnull
	Collection<Account> loadRealms();

	void deleteAllRealms();

	void updateRealm(@Nonnull Account account) throws RealmException;

	@Nonnull
	Collection<Account> loadRealmsInState(@Nonnull AccountState state);

	void init();
}
