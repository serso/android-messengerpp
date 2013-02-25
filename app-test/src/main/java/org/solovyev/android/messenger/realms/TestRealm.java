package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;

public class TestRealm extends AbstractRealm {

    @NotNull
    public static final String REALM_ID = "test";

    private TestRealm() {
        super(REALM_ID, null, null, null);
    }

    @NotNull
    @Override
    public RealmConnection createRealmConnection(@NotNull Context context) {
        return null;
    }

    @NotNull
    public static RealmEntity newEntity(@NotNull String realmEntityId) {
       return RealmEntityImpl.newInstance(REALM_ID, realmEntityId);
    }
}
