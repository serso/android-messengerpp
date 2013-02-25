package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;

public interface Realm {

    @NotNull
    String getId();

    @NotNull
    RealmDef getRealmDef();

    @NotNull
    User getUser();
}
