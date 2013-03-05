package org.solovyev.android.messenger.realms.vk;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.longpoll.LongPollRealmConnection;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.vk.chats.VkRealmChatService;
import org.solovyev.android.messenger.realms.vk.longpoll.VkRealmLongPollService;
import org.solovyev.android.messenger.realms.vk.users.VkRealmUserService;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public class VkRealm extends AbstractRealm<VkRealmConfiguration> {

    public VkRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull VkRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @Nonnull
    @Override
    public RealmConnection createRealmConnection(@Nonnull Context context) {
        return new LongPollRealmConnection(this, context, new VkRealmLongPollService(this));
    }

    @Nonnull
    @Override
    public String getDisplayName(@Nonnull Context context) {
        final StringBuilder sb = new StringBuilder();

        sb.append(context.getText(getRealmDef().getNameResId()));
        sb.append("(");
        sb.append(getUser().getDisplayName());
        sb.append(")");

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
