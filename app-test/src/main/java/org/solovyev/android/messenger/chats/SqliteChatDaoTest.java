package org.solovyev.android.messenger.chats;

import android.test.AndroidTestCase;
import junit.framework.Assert;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerDbConfiguration;
import org.solovyev.android.messenger.db.MessengerSQLiteOpenHelper;
import org.solovyev.android.messenger.realms.TestRealmDef;

import java.util.ArrayList;

public class SqliteChatDaoTest extends AndroidTestCase {

    public void setUp() throws Exception {
        super.setUp();
        getContext().deleteDatabase(MessengerApplication.DB_NAME);
    }

    public void testChatOperation() throws Exception {
        final ChatDao chatDao = new SqliteChatDao(getContext(), new MessengerSQLiteOpenHelper(getContext(), new MessengerDbConfiguration()));

        final ArrayList<ApiChat> chats = new ArrayList<ApiChat>();
        chatDao.mergeUserChats("test_01", chats);

        Assert.assertTrue(chatDao.loadUserChats("test_01").isEmpty());

        chats.add(ApiChatImpl.newInstance(TestRealmDef.newEntity("test_01"), 10, false));
        chats.add(ApiChatImpl.newInstance(TestRealmDef.newEntity("test_02"), 10, false));
        chats.add(ApiChatImpl.newInstance(TestRealmDef.newEntity("test_03"), 10, false));
        chats.add(ApiChatImpl.newInstance(TestRealmDef.newEntity("test_04"), 10, false));
        chatDao.mergeUserChats("test_01", chats);

        Chat chat = chatDao.loadChatById(TestRealmDef.newEntity("test_04").getEntityId());
        Assert.assertNotNull(chat);
        Assert.assertEquals(TestRealmDef.newEntity("test_04").getEntityId(), chat.getId());


    }
}
