package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:02 AM
 */
public class DefaultRealmService implements RealmService {

    @NotNull
    private final Map<String, Realm> realms = new HashMap<String, Realm>();

    public DefaultRealmService(@NotNull List<? extends Realm> realms) {
        for (Realm realm : realms) {
            this.realms.put(realm.getId(), realm);
        }
    }

    @NotNull
    @Override
    public Collection<Realm> getRealms() {
        return Collections.unmodifiableCollection(this.realms.values());
    }

    @NotNull
    @Override
    public Realm getRealmById(@NotNull String realmId) throws UnsupportedRealmException {
        final Realm realm = this.realms.get(realmId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmId);
        }
        return realm;
    }
}
