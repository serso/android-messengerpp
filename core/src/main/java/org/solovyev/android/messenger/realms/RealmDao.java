package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface RealmDao {

    @NotNull
    Realm insertRealm(@NotNull Realm realm);

    @NotNull
    Collection<Realm> loadRealms();
}
