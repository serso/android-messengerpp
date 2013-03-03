package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

public class RealmChangedEvent extends AbstractRealmEvent {

    public RealmChangedEvent(@Nonnull Realm realm) {
        super(realm);
    }
}
