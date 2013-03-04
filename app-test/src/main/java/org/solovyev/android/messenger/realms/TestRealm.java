package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserImpl;

import javax.annotation.Nonnull;

@Singleton
public class TestRealm extends AbstractRealm<TestRealmConfiguration> {

    @Inject
    public TestRealm(@Nonnull TestRealmDef realmDef) {
        super(realmDef.getId() + "~1", realmDef, UserImpl.newFakeInstance(RealmEntityImpl.newInstance(realmDef.getId() + "~1", "user01")), new TestRealmConfiguration("test_field", 42));
    }


    public TestRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull TestRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @Nonnull
    @Override
    public RealmConnection createRealmConnection(@Nonnull Context context) {
        return new TestRealmConnection(this);
    }

    @Nonnull
    @Override
    public String getDisplayName(@Nonnull Context context) {
        return context.getString(getRealmDef().getNameResId());
    }

    @Nonnull
    @Override
    public RealmUserService getRealmUserService() {
        return new TestRealmService();
    }
}
