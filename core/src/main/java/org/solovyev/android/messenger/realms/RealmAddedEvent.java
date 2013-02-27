package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

public class RealmAddedEvent extends AbstractRealmEvent {

    public RealmAddedEvent(@NotNull Realm realm) {
        super(realm);
    }
}
