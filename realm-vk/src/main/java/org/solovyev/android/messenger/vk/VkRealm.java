package org.solovyev.android.messenger.vk;

import android.content.Context;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.User;

public class VkRealm extends AbstractRealm<VkRealmConfiguration> {

    public VkRealm(@Nonnull String id, @Nonnull RealmDef realmDef, @Nonnull User user, @Nonnull VkRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
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
}
