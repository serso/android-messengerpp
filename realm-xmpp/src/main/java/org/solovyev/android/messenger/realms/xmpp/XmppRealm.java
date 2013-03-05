package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

public class XmppRealm extends AbstractRealm<XmppRealmConfiguration> {

    public XmppRealm(@Nonnull String id,
                     @Nonnull RealmDef realmDef,
                     @Nonnull User user,
                     @Nonnull XmppRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @Nonnull
    @Override
    public RealmConnection createRealmConnection(@Nonnull Context context) {
        return new XmppRealmConnection(this, context);
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

    @Nonnull
    @Override
    public RealmUserService getRealmUserService() {
        return new XmppRealmUserService(this);
    }

    @Nonnull
    @Override
    public RealmChatService getRealmChatService() {
        return new XmppRealmChatService(this);
    }
}
