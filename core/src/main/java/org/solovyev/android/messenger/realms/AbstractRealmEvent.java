package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractRealmEvent implements RealmEvent {

    @NotNull
    private final Realm realm;

    protected AbstractRealmEvent(@NotNull Realm realm) {
        this.realm = realm;
    }

    @NotNull
    @Override
    public Realm getRealm() {
        return realm;
    }
}
