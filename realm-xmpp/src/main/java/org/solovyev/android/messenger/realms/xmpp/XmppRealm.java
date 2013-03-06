package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class XmppRealm extends AbstractRealm<XmppRealmConfiguration> {

    private static final String TAG = XmppRealm.class.getSimpleName();

    public XmppRealm(@Nonnull String id,
                     @Nonnull RealmDef realmDef,
                     @Nonnull User user,
                     @Nonnull XmppRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @Nonnull
    @Override
    protected RealmConnection newRealmConnection0(@Nonnull Context context) {
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
        return new XmppRealmUserService(this, getXmppConnectionAware());
    }

    @Nonnull
    private XmppConnectionAware getXmppConnectionAware() {
        XmppConnectionAware realmAware = getRealmConnection();
        if ( realmAware == null ) {
            realmAware = TemporaryXmppConnectionAware.newInstance(this);
            Log.w(TAG, "Creation of temporary xmpp connection!");
        }
        return realmAware;
    }

    @Nullable
    protected XmppRealmConnection getRealmConnection() {
        return (XmppRealmConnection) super.getRealmConnection();
    }

    @Nonnull
    @Override
    public RealmChatService getRealmChatService() {
        return new XmppRealmChatService(this, getXmppConnectionAware());
    }
}
