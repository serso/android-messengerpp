package org.solovyev.android.messenger.realms.xmpp;

import com.google.inject.Inject;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.realms.RealmEntityImpl;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.UserImpl;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 8:28 PM
 */
public class XmppRealmUserServiceTest extends AbstractMessengerTestCase {

    @Nonnull
    private XmppRealm realm;

    @Inject
    @Nonnull
    private XmppRealmDef xmppRealmDef;


    @Nonnull
    private RealmUserService realmUserService;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        realm = newRealm();
        realmUserService = new XmppRealmUserService(realm);
    }

    public void testGetUserById() throws Exception {
        //realmUserService.getUserById(realm.getUser().getRealmUser().getRealmEntityId());
    }

    public void testGetUserContacts() throws Exception {

    }

    public void testCheckOnlineUsers() throws Exception {

    }

    public void testGetUserProperties() throws Exception {

    }


    @Nonnull
    protected XmppRealm newRealm() {
        final String realmId = xmppRealmDef.getId() + "~01";
        return new XmppRealm(realmId, xmppRealmDef, UserImpl.newFakeInstance(RealmEntityImpl.newInstance(realmId, TestXmppConfiguration.USER_LOGIN)), TestXmppConfiguration.getInstance());
    }
}
