package org.solovyev.android.messenger.realms;

import android.content.Context;
import com.google.inject.Singleton;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.test.R;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@Singleton
public class TestRealmDef extends AbstractRealmDef {

    @Nonnull
    public static final String REALM_ID = "test";

    public TestRealmDef() {
        super(REALM_ID, R.string.mpp_test_realm_name, R.drawable.mpp_test_icon, TestRealmConfigurationFragment.class, TestRealmConfiguration.class, false);
    }

    @Nonnull
    public RealmConnection createRealmConnection(@Nonnull Context context, @Nonnull Realm realm) {
        return null;
    }

    @Nonnull
    public static RealmEntity newEntity(@Nonnull String realmEntityId) {
       return RealmEntityImpl.newInstance(REALM_ID, realmEntityId);
    }

    @Nonnull
    @Override
    public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration) {
        return new TestRealm(realmId, this, user, (TestRealmConfiguration) configuration);
    }

    @Nonnull
    @Override
    public RealmBuilder newRealmBuilder(@Nonnull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        return Collections.emptyList();
    }

}
