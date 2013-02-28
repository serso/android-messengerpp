package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

public class RealmChangedEvent extends AbstractRealmEvent {

    public RealmChangedEvent(@NotNull Realm realm) {
        super(realm);
    }
}
