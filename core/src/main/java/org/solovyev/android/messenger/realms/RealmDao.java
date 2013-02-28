package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface RealmDao {

    void insertRealm(@NotNull Realm realm);

    void deleteRealm(@NotNull String realmId);

    @NotNull
    Collection<Realm> loadRealms();

    void deleteAllRealms();

    void updateRealm(@NotNull Realm realm);
}
