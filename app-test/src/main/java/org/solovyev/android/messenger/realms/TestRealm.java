package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;

@Singleton
public class TestRealm extends AbstractRealm<TestRealmConfiguration> {

    @Inject
    public TestRealm(@NotNull TestRealmDef realmDef) {
        super(realmDef.getId() + "~1", realmDef, UserImpl.newFakeInstance(RealmEntityImpl.newInstance(realmDef.getId() + "~1", "user01")), new TestRealmConfiguration("test_field", 42));
    }


    public TestRealm(@NotNull String id, @NotNull RealmDef realmDef, @NotNull User user, @NotNull TestRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }
}
