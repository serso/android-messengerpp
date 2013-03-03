package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public abstract class AbstractRealmEvent implements RealmEvent {

    @Nonnull
    private final Realm realm;

    protected AbstractRealmEvent(@Nonnull Realm realm) {
        this.realm = realm;
    }

    @Nonnull
    @Override
    public Realm getRealm() {
        return realm;
    }
}
