package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:09 PM
 */
public class XmppRealm implements RealmDef {

    @NotNull
    static final String REALM_ID = "xmpp";

    @NotNull
    private final ConnectionConfiguration configuration;

    @NotNull
    private final String id;

    public XmppRealm(@NotNull ConnectionConfiguration configuration, @NotNull String subRealm, @NotNull Context context) {
        this.configuration = configuration;
        this.id = REALM_ID + "-" + subRealm;
    }

    @NotNull
    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public int getNameResId() {
        return R.string.mpp_xmpp_name;
    }

    @Override
    public int getIconResId() {
        return R.drawable.mpp_xmpp_icon;
    }

    @NotNull
    @Override
    public RealmUserService newRealmUserService(@NotNull Realm realm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmChatService newRealmChatService(@NotNull Realm realm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmAuthService newRealmAuthService(@NotNull Realm realm) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public RealmConnection newRealmConnection(@NotNull Realm realm, @NotNull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
