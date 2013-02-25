package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.test.R;

public class TestRealmDef extends AbstractRealmDef {

    @NotNull
    public static final String REALM_ID = "test";

    private TestRealmDef() {
        super(REALM_ID, R.string.mpp_test_realm_name, R.drawable.mpp_test_icon, null, null, null);
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
