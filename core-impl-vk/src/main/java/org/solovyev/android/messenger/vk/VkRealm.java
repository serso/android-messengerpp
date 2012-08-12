package org.solovyev.android.messenger.vk;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.longpoll.LongPollRealmConnection;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.vk.chats.VkRealmChatService;
import org.solovyev.android.messenger.vk.longpoll.VkRealmLongPollService;
import org.solovyev.android.messenger.vk.secutiry.VkRealmAuthService;
import org.solovyev.android.messenger.vk.users.VkRealmUserService;

/**
* User: serso
* Date: 8/12/12
* Time: 10:34 PM
*/
public class VkRealm extends AbstractRealm {

    public VkRealm() {
        super(VkMessengerApplication.REALM_ID, new VkRealmUserService(), new VkRealmChatService(), new VkRealmAuthService());
    }

    @NotNull
    @Override
    public RealmConnection createRealmConnection(@NotNull Context context) {
        return new LongPollRealmConnection(this, context, new VkRealmLongPollService());
    }
}
