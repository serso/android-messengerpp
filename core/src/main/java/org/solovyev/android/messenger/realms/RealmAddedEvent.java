package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmAddedEvent extends AbstractRealmEvent {

    public RealmAddedEvent(@Nonnull Realm realm) {
        super(realm);
    }
}
