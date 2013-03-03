package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
* User: serso
* Date: 3/1/13
* Time: 9:31 PM
*/
public final class RealmFragmentFinishedEvent {

    @Nonnull
    private Realm realm;

    private boolean removed = false;

    public RealmFragmentFinishedEvent(@Nonnull Realm realm, boolean removed) {
        this.realm = realm;
        this.removed = removed;
    }

    @Nonnull
    public Realm getRealm() {
        return realm;
    }

    public boolean isRemoved() {
        return removed;
    }
}
