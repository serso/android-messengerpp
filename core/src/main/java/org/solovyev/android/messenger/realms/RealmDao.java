package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

import java.util.Collection;

public interface RealmDao {

    void insertRealm(@Nonnull Realm realm);

    void deleteRealm(@Nonnull String realmId);

    @Nonnull
    Collection<Realm> loadRealms();

    void deleteAllRealms();

    void updateRealm(@Nonnull Realm realm);
}
