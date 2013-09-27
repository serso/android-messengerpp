package org.solovyev.android.messenger;

import android.app.Application;

import javax.annotation.Nonnull;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.solovyev.android.messenger.accounts.AccountService;

import com.google.inject.Inject;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 10:16 PM
 */
@RunWith(RobolectricTestRunner.class)
public abstract class AbstractMessengerTestCase {

	@Nonnull
	private Application application = Robolectric.application;

	@Nonnull
	@Inject
	private AccountService accountService;

	@Nonnull
	private TestMessengerModule module;

	@Before
	public void setUp() throws Exception {
		module = new TestMessengerModule(application);
		module.setUp(this, module);
	}

	@After
	public void tearDown() throws Exception {
		module.tearDown();
	}

	@Nonnull
	public Application getApplication() {
		return application;
	}

	@Nonnull
	public AccountService getAccountService() {
		return accountService;
	}
}
