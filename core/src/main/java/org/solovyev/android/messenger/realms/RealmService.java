package org.solovyev.android.messenger.realms;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountConfiguration;

public interface RealmService {

	void init();

	/**
	 * @return collection of all configured realms in application
	 */
	@Nonnull
	Collection<Realm<? extends AccountConfiguration>> getRealms();

	/**
	 * Method returns the realm which previously has been registered in this service
	 *
	 *
	 * @param realmId id of realm
	 * @return realm
	 * @throws UnsupportedRealmException if realm hasn't been registered in this service
	 */
	@Nonnull
	Realm<? extends AccountConfiguration> getRealmById(@Nonnull String realmId) throws UnsupportedRealmException;
}
