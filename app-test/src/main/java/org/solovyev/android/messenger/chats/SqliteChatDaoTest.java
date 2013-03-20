package org.solovyev.android.messenger.chats;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.realms.TestRealmDef;
import org.solovyev.android.messenger.users.UserDao;
import org.solovyev.android.messenger.users.Users;

import java.util.ArrayList;

public class SqliteChatDaoTest extends AbstractMessengerTestCase {

    @Inject
    private UserDao userDao;

    @Inject
    private ChatDao chatDao;

    @Inject
    private TestRealmDef testRealmDef;

    @Inject
    private TestRealm testRealm;

    public void setUp() throws Exception {
        super.setUp();
        chatDao.deleteAllChats();
    }

    public void testChatOperations() throws Exception {

        final ArrayList<ApiChat> chats = new ArrayList<ApiChat>();

        final Entity realmUser = testRealm.newUserEntity("01");
        final String userId = realmUser.getEntityId();

        userDao.insertUser(Users.newEmptyUser(realmUser));

        chatDao.mergeUserChats(userId, chats);

        Assert.assertTrue(chatDao.loadUserChats(userId).isEmpty());

        chats.add(ApiChatImpl.newInstance(testRealm.newChatEntity("01"), 10, false));
        chats.add(ApiChatImpl.newInstance(testRealm.newChatEntity("02"), 10, false));
        chats.add(ApiChatImpl.newInstance(testRealm.newChatEntity("03"), 10, false));
        final Entity realmChat4 = testRealm.newChatEntity("04");
        chats.add(ApiChatImpl.newInstance(realmChat4, 10, false));
        chatDao.mergeUserChats(userId, chats);

        Chat chat = chatDao.loadChatById(realmChat4.getEntityId());
        Assert.assertNotNull(chat);
        Assert.assertEquals(realmChat4.getEntityId(), chat.getEntity().getEntityId());
    }

    @Override
    public void tearDown() throws Exception {
        chatDao.deleteAllChats();
        super.tearDown();
    }
}
