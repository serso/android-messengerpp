package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:09 PM
 */
public class XmppRealm implements Realm {

    @NotNull
    static final String REALM_ID = "xmpp";

    @NotNull
    private XmppRealmConnection xmppRealmConnection;

    @NotNull
    private final String id;

    public XmppRealm(@NotNull ConnectionConfiguration configuration, @NotNull String subRealm, @NotNull Context context) {
        id = REALM_ID + "-" + subRealm;
        xmppRealmConnection = new XmppRealmConnection(this, context, configuration);
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public RealmConnection createRealmConnection(@NotNull Context context) {
        return xmppRealmConnection;
    }

    @NotNull
    @Override
    public RealmUserService getRealmUserService() {
        return new XmppRealmUserService(xmppRealmConnection);
    }

    @NotNull
    @Override
    public RealmChatService getRealmChatService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmAuthService getRealmAuthService() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
