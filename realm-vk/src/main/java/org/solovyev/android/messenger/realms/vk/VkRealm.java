package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmState;
import org.solovyev.android.messenger.realms.vk.chats.VkRealmChatService;
import org.solovyev.android.messenger.realms.vk.users.VkRealmUserService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public class VkRealm extends AbstractRealm<VkRealmConfiguration> {

    public VkRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull VkRealmConfiguration configuration, @Nonnull RealmState state) {
        super(id, realmDef, user, configuration, state);
    }

    @Nonnull
    @Override
    protected RealmConnection newRealmConnection0(@Nonnull Context context) {
        return new VkLongPollRealmConnection(this, context);
    }

    @Nonnull
    @Override
    public String getDisplayName(@Nonnull Context context) {
        final StringBuilder sb = new StringBuilder();

        sb.append(context.getText(getRealmDef().getNameResId()));

        return sb.toString();
    }

    @Nonnull
    @Override
    public RealmUserService getRealmUserService() {
        return new VkRealmUserService(this);
    }

    @Nonnull
    @Override
    public RealmChatService getRealmChatService() {
        return new VkRealmChatService(this);
    }
}
