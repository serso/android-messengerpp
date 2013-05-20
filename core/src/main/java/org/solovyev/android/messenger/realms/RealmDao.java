package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface RealmDao {

	final String TAG = RealmDao.class.getSimpleName();

	void insertRealm(@Nonnull Realm realm) throws RealmException;

	void deleteRealm(@Nonnull String realmId);

	@Nonnull
	Collection<Realm> loadRealms();

	void deleteAllRealms();

	void updateRealm(@Nonnull Realm realm) throws RealmException;

	@Nonnull
	Collection<Realm> loadRealmsInState(@Nonnull RealmState state);

	void init();
}
