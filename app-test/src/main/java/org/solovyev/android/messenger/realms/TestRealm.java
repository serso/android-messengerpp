package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;

public class TestRealm extends AbstractRealm<TestRealmConfiguration> {

    public TestRealm(@NotNull String id, @NotNull RealmDef realmDef, @NotNull User user, @NotNull TestRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }
}
