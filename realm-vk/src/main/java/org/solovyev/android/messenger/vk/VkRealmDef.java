package org.solovyev.android.messenger.vk;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.longpoll.LongPollRealmConnection;
import org.solovyev.android.messenger.realms.AbstractRealmDef;
import org.solovyev.android.messenger.vk.chats.VkRealmChatService;
import org.solovyev.android.messenger.vk.longpoll.VkRealmLongPollService;
import org.solovyev.android.messenger.vk.secutiry.VkRealmAuthService;
import org.solovyev.android.messenger.vk.users.VkRealmUserService;

/**
* User: serso
* Date: 8/12/12
* Time: 10:34 PM
*/
public class VkRealmDef extends AbstractRealmDef {

    @NotNull
    public static final String REALM_ID = "vk";

    public VkRealmDef() {
        super(REALM_ID, R.string.mpp_vk_realm_name, R.drawable.mpp_vk_icon, new VkRealmUserService(), new VkRealmChatService(), new VkRealmAuthService());
    }

    @NotNull
    @Override
    public RealmConnection createRealmConnection(@NotNull Context context) {
        return new LongPollRealmConnection(this, context, new VkRealmLongPollService());
    }
}
