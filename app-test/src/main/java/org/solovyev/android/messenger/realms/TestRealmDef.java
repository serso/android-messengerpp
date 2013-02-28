package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.test.R;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

@Singleton
public class TestRealmDef extends AbstractRealmDef {

    @NotNull
    public static final String REALM_ID = "test";

    public TestRealmDef() {
        super(REALM_ID, R.string.mpp_test_realm_name, R.drawable.mpp_test_icon, TestRealmConfigurationFragment.class, TestRealmConfiguration.class);
    }

    @NotNull
    public RealmConnection createRealmConnection(@NotNull Context context, @NotNull Realm realm) {
        return null;
    }

    @NotNull
    public static RealmEntity newEntity(@NotNull String realmEntityId) {
       return RealmEntityImpl.newInstance(REALM_ID, realmEntityId);
    }

    @NotNull
    @Override
    public RealmUserService newRealmUserService(@NotNull Realm realm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmChatService newRealmChatService(@NotNull Realm realm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmConnection newRealmConnection(@NotNull Realm realm, @NotNull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public Realm newRealm(@NotNull String realmId, @NotNull User user, @NotNull RealmConfiguration configuration) {
        return new TestRealm(realmId, this, user, (TestRealmConfiguration) configuration);
    }

    @NotNull
    @Override
    public RealmBuilder newRealmBuilder(@NotNull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
