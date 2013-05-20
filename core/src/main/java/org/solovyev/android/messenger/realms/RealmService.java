package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 12:57 AM
 */
public interface RealmService {

	@Nonnull
	static String TAG = "RealmService";

	/**
	 * Method initializes service, must be called once before any other operations with current service
	 */
	void init();

	/**
	 * Method restores service state (e.g. loads persistence data from database)
	 */
	void load();


	/**
	 * @return collection of all configured realms in application
	 */
	@Nonnull
	Collection<RealmDef> getRealmDefs();

	@Nonnull
	Collection<Realm> getRealms();

	@Nonnull
	Collection<Realm> getEnabledRealms();

	/**
	 * @return collection of users in all configured realms
	 */
	@Nonnull
	Collection<User> getRealmUsers();

	/**
	 * @return collection of users in all configured ENABLED realms
	 */
	@Nonnull
	Collection<User> getEnabledRealmUsers();

	/**
	 * Method returns the realm which previously has been registered in this service
	 *
	 * @param realmDefId id of realm def
	 * @return realm
	 * @throws UnsupportedRealmException if realm hasn't been registered in this service
	 */
	@Nonnull
	RealmDef getRealmDefById(@Nonnull String realmDefId) throws UnsupportedRealmException;

	@Nonnull
	Realm getRealmById(@Nonnull String realmId) throws UnsupportedRealmException;

	@Nonnull
	Realm getRealmByEntity(@Nonnull Entity entity) throws UnsupportedRealmException;

	@Nonnull
	Realm saveRealm(@Nonnull RealmBuilder realmBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException;

	@Nonnull
	Realm changeRealmState(@Nonnull Realm realm, @Nonnull RealmState newState);

	void removeRealm(@Nonnull String realmId);

	boolean isOneRealm();

    /*
	**********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

	void addListener(@Nonnull JEventListener<RealmEvent> listener);

	void removeListener(@Nonnull JEventListener<RealmEvent> listener);

	void stopAllRealmConnections();

	List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context);
}
