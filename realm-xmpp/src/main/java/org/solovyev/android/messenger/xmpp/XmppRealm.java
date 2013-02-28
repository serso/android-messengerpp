package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.User;

public class XmppRealm extends AbstractRealm<XmppRealmConfiguration> {

    public XmppRealm(@NotNull String id,
                     @NotNull RealmDef realmDef,
                     @NotNull User user,
                     @NotNull XmppRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @NotNull
    @Override
    public String getDisplayName(@NotNull Context context) {
        final StringBuilder sb = new StringBuilder();

        sb.append(context.getText(getRealmDef().getNameResId()));
        sb.append("(");
        sb.append(getConfiguration().getServer());
        sb.append(", ");
        sb.append(getConfiguration().getLogin());
        sb.append(")");

        return sb.toString();
    }
}
