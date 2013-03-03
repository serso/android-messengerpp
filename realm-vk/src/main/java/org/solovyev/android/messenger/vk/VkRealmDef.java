package org.solovyev.android.messenger.vk;

import android.content.Context;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.longpoll.LongPollRealmConnection;
import org.solovyev.android.messenger.realms.AbstractRealmDef;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.vk.chats.VkRealmChatService;
import org.solovyev.android.messenger.vk.longpoll.VkRealmLongPollService;
import org.solovyev.android.messenger.vk.users.VkRealmUserService;

/**
* User: serso
* Date: 8/12/12
* Time: 10:34 PM
*/
@Singleton
public class VkRealmDef extends AbstractRealmDef {

    @Nonnull
    private static final String REALM_ID = "vk";

    public VkRealmDef() {
        super(REALM_ID, R.string.mpp_vk_realm_name, R.drawable.mpp_vk_icon, VkRealmConfigurationFragment.class, VkRealmConfiguration.class);
    }

    @Nonnull
    @Override
    public RealmConnection newRealmConnection(@Nonnull Realm realm, @Nonnull Context context) {
        return new LongPollRealmConnection(realm, context, new VkRealmLongPollService(realm));
    }

    @Nonnull
    @Override
    public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration) {
        return new VkRealm(realmId, this, user, (VkRealmConfiguration) configuration);
    }

    @Nonnull
    @Override
    public RealmBuilder newRealmBuilder(@Nonnull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Nonnull
    @Override
    public RealmUserService newRealmUserService(@Nonnull Realm realm) {
        return new VkRealmUserService(realm);
    }

    @Nonnull
    @Override
    public RealmChatService newRealmChatService(@Nonnull Realm realm) {
        return new VkRealmChatService(realm);
    }
/*
    @Nonnull
    @Override
    public RealmAuthService newRealmAuthService(@Nonnull Realm realm) {
        return new VkRealmAuthService(login, password);
    }*/
}
