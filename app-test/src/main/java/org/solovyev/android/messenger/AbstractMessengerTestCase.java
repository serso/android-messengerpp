package org.solovyev.android.messenger;

import android.app.Application;
import android.test.InstrumentationTestCase;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.RealmService;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 10:16 PM
 */
public abstract class AbstractMessengerTestCase extends InstrumentationTestCase {

    @NotNull
    @Inject
    private Application application;

    @NotNull
    @Inject
    private RealmService realmService;

    @NotNull
    private TestMessengerModule module;

    public void setUp() throws Exception {
        super.setUp();
        Thread.sleep(100);
        final Application applicationContext = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        module = new TestMessengerModule(applicationContext);
        module.setUp(this, module);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        module.tearDown();
    }

/*    @NotNull
    public Application getApplication() {
        return application;
    }*/

    @NotNull
    public RealmService getRealmService() {
        return realmService;
    }
}
