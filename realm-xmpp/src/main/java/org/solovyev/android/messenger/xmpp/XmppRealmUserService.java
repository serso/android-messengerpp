package org.solovyev.android.messenger.xmpp;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jivesoftware.smack.Connection;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import java.util.List;

/**
* User: serso
* Date: 2/24/13
* Time: 8:45 PM
*/
class XmppRealmUserService implements RealmUserService {

    @NotNull
    private final XmppRealmConnection xmppConnection;

    XmppRealmUserService(@NotNull XmppRealmConnection xmppConnection) {
        this.xmppConnection = xmppConnection;
    }

    @Nullable
    @Override
    public User getUserById(@NotNull String realmUserId) {
        final Connection connection = xmppConnection.getConnection();
        //ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(this.xmppConnection);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public List<User> getUserContacts(@NotNull String realmUserId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public List<User> checkOnlineUsers(@NotNull List<User> users) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public List<AProperty> getUserProperties(@NotNull User user, @NotNull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
