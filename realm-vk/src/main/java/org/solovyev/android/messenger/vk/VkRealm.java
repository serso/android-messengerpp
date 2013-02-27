package org.solovyev.android.messenger.vk;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.User;

public class VkRealm extends AbstractRealm<VkRealmConfiguration> {

    public VkRealm(@NotNull String id, @NotNull RealmDef realmDef, @NotNull User user, @NotNull VkRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }
}
