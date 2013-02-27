package org.solovyev.android.messenger;

import android.app.Application;
import android.test.AndroidTestCase;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.RealmService;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 10:16 PM
 */
public abstract class AbstractMessengerTestCase extends AndroidTestCase {

    @NotNull
    @Inject
    private Application application;

    @NotNull
    @Inject
    private RealmService realmService;

    @NotNull
    private final TestMessengerModule module = new TestMessengerModule(MessengerApplication.getInstance());

    public void setUp() throws Exception {
        super.setUp();
        module.setUp(this, module);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        module.tearDown();
    }

    @NotNull
    public Application getApplication() {
        return application;
    }

    @NotNull
    public RealmService getRealmService() {
        return realmService;
    }
}
