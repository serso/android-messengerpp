package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

/**
* User: serso
* Date: 3/1/13
* Time: 9:31 PM
*/
public final class RealmFragmentFinishedEvent {

    @NotNull
    private Realm realm;

    private boolean removed = false;

    public RealmFragmentFinishedEvent(@NotNull Realm realm, boolean removed) {
        this.realm = realm;
        this.removed = removed;
    }

    @NotNull
    public Realm getRealm() {
        return realm;
    }

    public boolean isRemoved() {
        return removed;
    }
}
