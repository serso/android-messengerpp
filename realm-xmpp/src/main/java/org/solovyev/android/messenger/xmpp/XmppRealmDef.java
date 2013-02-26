package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.realms.AbstractRealmDef;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.RealmUserService;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:09 PM
 */
public class XmppRealmDef extends AbstractRealmDef {

    @NotNull
    static final String REALM_ID = "xmpp";

    public XmppRealmDef() {
        super(REALM_ID, R.string.mpp_xmpp_name, R.drawable.mpp_xmpp_icon, XmppRealmConfigurationActivity.class);
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
