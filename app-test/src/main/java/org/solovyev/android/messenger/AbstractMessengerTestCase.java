package org.solovyev.android.messenger;

import android.app.Application;
import android.test.InstrumentationTestCase;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.realms.RealmService;

import com.google.inject.Inject;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 10:16 PM
 */
public abstract class AbstractMessengerTestCase extends InstrumentationTestCase {

	@Nonnull
	@Inject
	private Application application;

	@Nonnull
	@Inject
	private AccountService accountService;

	@Nonnull
	@Inject
	private RealmService realmService;

	@Nonnull
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

/*    @Nonnull
	public Application getApplication() {
        return application;
    }*/

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	public RealmService getRealmService() {
		return realmService;
	}
}
