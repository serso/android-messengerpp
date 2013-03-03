package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.common.listeners.JEventListener;

import java.util.Collection;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 12:57 AM
 */
public interface RealmService {

    @Nonnull
    static String TAG = "RealmService";

    /**
     * @return collection of all configured realms in application
     */
    @Nonnull
    Collection<RealmDef> getRealmDefs();

    @Nonnull
    Collection<Realm> getRealms();

    /**
     * Method returns the realm which previously has been registered in this service
     * @param realmDefId id of realm def
     * @return realm
     * @throws UnsupportedRealmException if realm hasn't been registered in this service
     */
    @Nonnull
    RealmDef getRealmDefById(@Nonnull String realmDefId) throws UnsupportedRealmException;

    @Nonnull
    Realm getRealmById(@Nonnull String realmId) throws UnsupportedRealmException;

    @Nonnull
    Realm saveRealm(@Nonnull RealmBuilder realmBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException;

    void removeRealm(@Nonnull String realmId);

    void load();

    /*
    **********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

    void addListener(@Nonnull JEventListener<? extends RealmEvent> listener);

    void removeListener(@Nonnull JEventListener<? extends RealmEvent> listener);
}
