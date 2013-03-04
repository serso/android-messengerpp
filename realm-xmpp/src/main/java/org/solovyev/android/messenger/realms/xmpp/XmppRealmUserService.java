package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    private final XmppRealmConnection xmppConnection;

    XmppRealmUserService(@Nonnull XmppRealmConnection xmppConnection) {
        this.xmppConnection = xmppConnection;
    }

    @Nullable
    @Override
    public User getUserById(@Nonnull String realmUserId) {
        final Connection connection = xmppConnection.getConnection();
        //ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(this.xmppConnection);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nonnull
    @Override
    public List<User> getUserContacts(@Nonnull String realmUserId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nonnull
    @Override
    public List<User> checkOnlineUsers(@Nonnull List<User> users) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
