package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.User;

public class XmppRealm extends AbstractRealm<XmppRealmConfiguration> {

    public XmppRealm(@Nonnull String id,
                     @Nonnull RealmDef realmDef,
                     @Nonnull User user,
                     @Nonnull XmppRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @Nonnull
    @Override
    public String getDisplayName(@Nonnull Context context) {
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
