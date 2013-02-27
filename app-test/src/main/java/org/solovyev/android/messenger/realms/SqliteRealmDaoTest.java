package org.solovyev.android.messenger.realms;

import android.app.Application;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.solovyev.android.messenger.MessengerDbConfiguration;
import org.solovyev.android.messenger.TestMessengerModule;
import org.solovyev.android.messenger.db.MessengerSQLiteOpenHelper;

@RunWith(RobolectricTestRunner.class)
public class SqliteRealmDaoTest {

    @Inject
    private Application application;

    @Before
    public void setUp() throws Exception {
        final TestMessengerModule module = new TestMessengerModule();

        TestMessengerModule.setUp(this, module);
    }

    @Test
    public void testRealmOperations() throws Exception {
        final RealmDao realmDao = new SqliteRealmDao(application, new MessengerSQLiteOpenHelper(application, new MessengerDbConfiguration()));
    }

    @After
    public void tearDown() {
        TestMessengerModule.tearDown();
    }
}
